require 'set'

class Component
	@@comps = Components.instance
	@@instanceClsMap = {:native => 'muscle.core.standalone.NativeKernel',
                      :mpi    => 'muscle.core.standalone.MPIKernel',
                      :matlab => 'muscle.core.standalone.MatlabKernel'}
  
	def initialize(name, cls, env = {})
    @name = name

    if @@instanceClsMap.has_key?(cls)
      @cls = @@instanceClsMap[cls]
    else
      @cls = cls
    end
		@env = {}
		@out = Set.new
		@in = Set.new
		@couplings = []
		env.each { |k, v|
			self[k] = v unless k.is_a? Symbol # exclude instance parameters
		}
		@@comps.add(self)
	end

	def ==(other)
		name == other.name
	end

	def to_s
		prop(cls)
	end

	def portal_name
		@name
	end
	
	def couple(other, ports, filters = [], exit_filters = nil)
		if not exit_filters.nil?
			filters.push('', *exit_filters)
		end
		filter_s = filters.empty? ? ' ' : '(' + filters.join(',') + ') '
		
		if not ports.is_a?(Hash)
			ports = {ports => ports}
		end
				
		ports.each { |from,to|
			raise "Port may not be empty" if from.nil? or from.empty? or to.nil? or to.empty?
			raise "There is already a connection to " << other.to_s << "." << to	if other.in.member?(to)
			raise "There is already a connection from " << to_s << "." << from if @out.member?(from)

			other.in.add(to)
			@out.add(from)
		
			couplings.push(to + '@' + other.portal_name + ' ->' + filter_s + from + '@' + portal_name)
		}
	end
	
	def [](key)
		env[prop(key)]
	end
	
	def []=(key, value)
    if value.nil?
      env.delete(prop(key))
    else
      env[prop(key)] = value      
    end
	end
  
  def prop(prop)
    name + ':' + prop
  end
	
	attr_reader :env, :in, :cls, :name, :couplings
end

class Instance < Component
end

class NativeInstance < Instance
  def initialize(name, command, env={})
    env[:java_class] = :native unless env[:java_class]
    super(name, env[:java_class], env)
    self['command'] = command
    self['args'] = env[:args]
  end
end

class MPIInstance < NativeInstance
  def initialize(name, command, env = {})
    env[:java_class] = :mpi unless env[:java_class]
    super(name, command, env)
    self['mpiexec_command'] = env[:mpiexec]
    self['mpiexec_args'] = env[:mpiexec_args]
  end
end

class MatlabInstance < Instance
  def initialize(name, script, env = {})
    env[:java_class] = :matlab unless env[:java_class]
    super(name, env[:java_class], env)    
    self['script'] = script
    self['args'] = env[:args]

    unless env[:matlab]
      env[:matlab] = MuscleUtils.which('matlab')
      abort 'Matlab interpreter not found in path. Specify with MatlabInstance.new(..., matlab: \'/path/to/matlab\')' if env[:matlab].nil?
    end
    
    self['matlab_command'] = env[:matlab]
    self['matlab_args'] = env[:matlab_args]
  end
end

class PythonInstance < NativeInstance
  def initialize(name, script, env = {})
    unless env[:python]
      env[:python] = MuscleUtils.which('python')
      abort 'Python interpreter not found in path. Specify with PythonInstance.new(..., python=\'/path/to/python\')' if env[:python].nil?
    end

    super(name, env[:python], env)

    self['command'] = env[:python]
    self['args'] = env[:args] ? script + ' ' + args : script
  end
end

class Terminal < Component
	def portal_name
		self.name + '(' + self.cls + ')'
	end
	
	def couple(other, ports, filters = [], exit_filters = nil)
		super
		raise "terminal has only one in- or output" if @out.size + @in.size > 1
	end
end
