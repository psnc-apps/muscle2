package muscle.util.logging;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.id.Identifier;

/**
 *
 * @author joris
 */
public abstract class ActivityWriter implements ActivityListener {
	private final long nano;
	private final static Logger logger = Logger.getLogger(ActivityWriter.class.getName());
	private boolean isDone;
	
	public ActivityWriter() {
		nano = System.nanoTime();
		isDone = false;
	}
	
	@Override
	public final void activity(ActivityProtocol action, String id) {
		final long diff = System.nanoTime() - nano;
		try {
			write(action, id, (int)(diff / 1000000000), (int)(diff % 1000000000));
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Cannot write to activity log", ex);
		}
	}
	
	@Override
	public final void activity(ActivityProtocol action, Identifier id) {
		activity(action, id.getName());
	}

	@Override
	public final void init() {
		try {
			init(nano / 1000000000L, (int)(nano % 1000000000));
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Cannot write to activity log", ex);
		}
	}
	
	protected abstract void init(long sec, int nano) throws IOException;

	@Override
	public final synchronized boolean isDisposed() {
		return isDone;
	}
	
	protected abstract void write(ActivityProtocol action, String id, int sec, int nano) throws IOException;
	
	@Override
	public final void dispose() {
		synchronized (this) {
			if (this.isDone) return;
			this.isDone = true;
		}

		long time = System.nanoTime();
		try {
			dispose(time / 1000000000L, (int)(time % 1000000000));
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Cannot close activity log", ex);
		}
	}
	
	protected abstract void dispose(long sec, int nano) throws IOException;
}
