class Components
	@@instance = nil
	
	def Components.instance		
		Components.new if @@instance.nil?
		return @@instance
	end
	
	def initialize
		raise "Can not create multiple Components" unless @@instance.nil?
		@@instance = self
		@terminals = {}
		@instances = {}
	end
	
	def add(comp)
		hash = comp.is_a?(Terminal) ? self.terminals : self.instances

		if hash.has_key?(comp.name)
			puts "Error: instance <#{comp.name}> is defined twice."
			exit 4
		end
		hash[comp.name] = comp
	end
	
	def get(name)
		inst = self.instances[name]
		if inst.nil?
			self.terminals[name]
		else
			inst
		end
 	end
	
	def coupling_s
		s = ""
		self.instances.each { |k,inst| s << inst.couplings.join("\n") << "\n" }
		self.terminals.each { |k,term| s << term.couplings.join("\n") << "\n" }
		return s
	end
	
	def to_s
		s = ""
		self.instances.each { |k,inst| s << inst.to_s << "\n" }
		self.terminals.each { |k,term| s << term.to_s << "\n" }
		return s
	end
	
	def env
		e = {}
		self.instances.each { |k,inst| e.merge!(inst.env)}
		self.terminals.each { |k,term| e.merge!(term.env)}
		return e
	end
	
	attr_reader :instances, :terminals
end
