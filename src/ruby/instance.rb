require 'set'

class Component
	@@comps = Components.instance
	
	def initialize(name, cls, args=[])
		@name = name
		@cls = cls
		@args = args
		@env = {}
		@out = Set.new
		@in = Set.new
		@couplings = []
		@@comps.add(self)
	end

	def ==(other)
		self.name == other.name
	end

	def to_s
		if self.args.empty?
			self.name + ':' + self.cls
		else
			self.name + ':' + self.cls + '(' << self.args.join(',') + ')'
		end
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
			raise "There is already a connection from " << self.to_s << "." << from if @out.member?(from)

			other.in.add(to)
			@out.add(from)
		
			self.couplings.push(to + '@' + other.portal_name + ' ->' + filter_s + from + '@' + self.portal_name)
		}
	end
	
	def [](key)
		self.env[self.name + ':' + key]
	end
	
	def []=(key, value)
		self.env[self.name + ':' + key] = value
	end
	
	attr_reader :env, :in, :cls, :name, :args, :couplings
end

class Instance < Component
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
