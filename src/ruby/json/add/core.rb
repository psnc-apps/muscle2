=begin
== Copyright and License
   Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium
   Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project

   GNU Lesser General Public License

   This file is part of MUSCLE (Multiscale Coupling Library and Environment).

   MUSCLE is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   MUSCLE is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
=end
# This file contains implementations of ruby core's custom objects for
# serialisation/deserialisation.

unless Object.const_defined?(:JSON) and ::JSON.const_defined?(:JSON_LOADED) and
  ::JSON::JSON_LOADED
  require 'json'
end
require 'date'

class Symbol
  def to_json(*a)
    {
      JSON.create_id => self.class.name,
      's' => to_s,
    }.to_json(*a)
  end

  def self.json_create(o)
    o['s'].to_sym
  end
end

class Time
  def self.json_create(object)
    if usec = object.delete('u') # used to be tv_usec -> tv_nsec
      object['n'] = usec * 1000
    end
    if respond_to?(:tv_nsec)
      at(*object.values_at('s', 'n'))
    else
      at(object['s'], object['n'] / 1000)
    end
  end

  def to_json(*args)
    {
      JSON.create_id => self.class.name,
      's' => tv_sec,
      'n' => respond_to?(:tv_nsec) ? tv_nsec : tv_usec * 1000
    }.to_json(*args)
  end
end

class Date
  def self.json_create(object)
    civil(*object.values_at('y', 'm', 'd', 'sg'))
  end

  alias start sg unless method_defined?(:start)

  def to_json(*args)
    {
      JSON.create_id => self.class.name,
      'y' => year,
      'm' => month,
      'd' => day,
      'sg' => start,
    }.to_json(*args)
  end
end

class DateTime
  def self.json_create(object)
    args = object.values_at('y', 'm', 'd', 'H', 'M', 'S')
    of_a, of_b = object['of'].split('/')
    if of_b and of_b != '0'
      args << Rational(of_a.to_i, of_b.to_i)
    else
      #
      # Only hack for ruby 1.8.5
      # Without the fix below, "rake test_ext (or rake test_pure) fails
      # for test_utc_datetime(TC_JSONAddition) at tests/test_json_addition.rb:156
      if RUBY_VERSION == "1.8.5"
        if of_a.is_a?(String)
          of_a = of_a.to_i
        end
      end
      # The above fix is not needed for ruby 1.8.6 and above due to the change
      # on civil() in date.rb
      # Fix for ruby 1.8.5 end
      #
      args << of_a
    end
    args << object['sg']
    civil(*args)
  end

  alias start sg unless method_defined?(:start)

  def to_json(*args)
    {
      JSON.create_id => self.class.name,
      'y' => year,
      'm' => month,
      'd' => day,
      'H' => hour,
      'M' => min,
      'S' => sec,
      'of' => offset.to_s,
      'sg' => start,
    }.to_json(*args)
  end
end

class Range
  def self.json_create(object)
    new(*object['a'])
  end

  def to_json(*args)
    {
      JSON.create_id   => self.class.name,
      'a'         => [ first, last, exclude_end? ]
    }.to_json(*args)
  end
end

class Struct
  def self.json_create(object)
    new(*object['v'])
  end

  def to_json(*args)
    klass = self.class.name
    klass.to_s.empty? and raise JSON::JSONError, "Only named structs are supported!"
    {
      JSON.create_id => klass,
      'v'     => values,
    }.to_json(*args)
  end
end

class Exception
  def self.json_create(object)
    result = new(object['m'])
    result.set_backtrace object['b']
    result
  end

  def to_json(*args)
    {
      JSON.create_id => self.class.name,
      'm'   => message,
      'b' => backtrace,
    }.to_json(*args)
  end
end

class Regexp
  def self.json_create(object)
    new(object['s'], object['o'])
  end

  def to_json(*)
    {
      JSON.create_id => self.class.name,
      'o' => options,
      's' => source,
    }.to_json
  end
end
