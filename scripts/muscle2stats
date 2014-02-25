#!/usr/bin/env ruby

def sec_s(nano_sec)
  (nano_sec / 1000000000.0).to_s
end
def perc_s(part, total)
  ((100*part)/total).to_i.to_s
end
def nano_i(sec, nano)
  sec.to_i * 1000000000 + nano.to_i
end
def milli_i(sec, milli)
  sec.to_i * 1000000000 + milli.to_i * 1000000
end

def read_logs(files)
  firstReg = /^(\w+) (\d+)\.(\d+) INIT .*\[(.*) \<.*$/
  midReg = /^(\w+) (\d+)\.(\d+) (\w+):(\w+(@\w+)?)$/
  lastReg = /^(\w+) (\d+)\.(\d+) FINALIZE$/

  data = []
  
  files.each do |log|
    abort "File #{log} not found" unless File.exists?(log)
    if File.directory?(log)
      log = "#{log}/activity.log"
      abort "File #{log} not found" unless File.exists?(log)
    end
    print "Parsing <#{log}>...\n"
    lines = File.read(log).each_line($/).to_a # not lines, gives enumerator in Ruby 1.9.3

    # parse first line separately
    fHash, fSec, fMilli, fLoc = firstReg.match(lines.shift).captures
    fSNano = milli_i(fSec, fMilli)
    data.push( {time: fSNano, op: "INIT", id: fLoc, hash: fHash} )
  
    # discard finalize
    lHash, lSec, lNano = lastReg.match(lines.pop).captures
    # add one to make sure the times are consistent
    lSNano = fSNano + nano_i(lSec, lNano.to_i)
    data.push( {time: lSNano, op: "FINALIZE", id: fLoc, hash: fHash} )
  
    lines.each do |line|
      mHash, mSec, mNano, mOp, mId = midReg.match(line).captures
      rNano = fSNano + nano_i(mSec, mNano)
      data.push( {time: rNano, op: mOp, id: mId, hash: fHash} )
    end
  end

  data.sort { |x,y| x[:time] <=> y[:time] }
end

def print_logs(data, min_time)
  data.each do |record|
    print "#{sec_s(record[:time] - min_time).ljust(8)} #{record[:op].rjust(15)}: #{record[:id].rjust(35)} (#{record[:hash]})\n"
  end
end

def organize(data)
  instances = {}

  idReg = /^(?<port>\w+)@(?<instance>\w+)$/
  data.each do |record|
    next if record[:op] == 'INIT' || record[:op] == 'FINALIZE'

    portId = idReg.match(record[:id])
    if portId
      instance = portId[:instance]
      key = (record[:op] =~ /RECEIVE/ ? "[in] #{portId[:port]}" : "[out] #{portId[:port]}")
    else
      instance = record[:id]
      key = :instance
    end
  
    instances[instance] = {} unless instances.has_key? instance

    if not instances[instance].has_key? key
      instances[instance][key] = {begin: [], end: [], connect: []}
      if portId
        instances[instance][key][:name] = portId[:port]
        instances[instance][key][:type] = (record[:op] =~ /RECEIVE/ ? :receive : :send)
      else
        instances[instance][key][:location] = record[:id]
      end
    end
  
    if record[:op] =~ /END_|STOP/
      opType = :end
    elsif record[:op] =~ /CONNECT/
      opType = :connect
    elsif record[:op] =~ /BEGIN_|START/
      opType = :begin
    end
  
    instances[instance][key][opType].push record[:time]
  end
  return instances
end

def print_statistics(instances, min_time, max_time)
  sim_time = max_time - min_time;

  print "\n== STATISTICS ==\n"
  print "Simulation time: #{sec_s(sim_time)}\n\n"

  instances.each do |name,instance|
    next unless instance[:instance]
    inst_begin = instance[:instance][:begin][0]
    inst_max = instance[:instance][:end].empty? ? max_time : instance[:instance][:end][0]
    inst_time = inst_max - inst_begin
    print "Instance '#{name}': #{sec_s(inst_time)} s (#{perc_s(inst_time,sim_time)}% of total)\n"
  
    send_comm = 0
    recv_comm = 0
  
    instance.each do |port,values|
      next if port == :instance
      comm = 0
      comm += max_time * (values[:begin].length - values[:end].length)
      values[:end].each { |x| comm += x }
      values[:begin].each { |x| comm -= x }
      if values[:type] == :send
        send_comm += comm
      else
        recv_comm += comm
      end
      instance[port][:comm] = comm
    end
    
    comp_time = inst_time - send_comm - recv_comm

    send_arr = []
    recv_arr = []
    comm_arr = []
    instance.each do |port,values|
      next if port == :instance
      if values[:type] == :send
        send_arr.push([values[:comm], "#{values[:name]}: #{sec_s(values[:comm])} (#{perc_s(values[:comm], send_comm)}%)"])
        comm_arr.push([values[:comm], "#{port}: #{sec_s(values[:comm])} (#{perc_s(values[:comm], recv_comm+send_comm)}%)"])
      else
        recv_arr.push([values[:comm], "#{values[:name]}: #{sec_s(values[:comm])} (#{perc_s(values[:comm], recv_comm)}%)"])
        comm_arr.push([values[:comm], "#{port}: #{sec_s(values[:comm])} (#{perc_s(values[:comm], recv_comm+send_comm)}%)"])
      end
    end
    send_arr.sort! { |x, y| y[0] <=> x[0] }
    recv_arr.sort! { |x, y| y[0] <=> x[0] }
    comm_arr.sort! { |x, y| y[0] <=> x[0] }
    
    print "\tComputing: #{sec_s(comp_time)} s (#{perc_s(comp_time,inst_time)}%)\n"
    print "\tSending: #{sec_s(send_comm)} s (#{perc_s(send_comm,inst_time)}%)\n"
    print "\t  -> Ports: #{send_arr.map{|x| x[1]}.join('; ')}\n" if send_arr.size > 0
    print "\tReceiving: #{sec_s(recv_comm)} s (#{perc_s(recv_comm,inst_time)}%)\n"
    print "\t  -> Ports: #{recv_arr.map{|x| x[1]}.join('; ')}\n" if recv_arr.size > 0
    print "\tTotal communication: #{sec_s(recv_comm+send_comm)} s (#{perc_s(recv_comm+send_comm,inst_time)}%)\n"
    print "\t  -> Ports: #{comm_arr.map{|x| x[1]}.join('; ')}\n" if comm_arr.size > 0
    print "\n"
  end
end

def parse_options
  opts = {}
  args = ARGV
  opts[:print_logs] = !args.delete('-p').nil?
  opts[:help] = !args.delete('-h').nil?
  
  if args.size == 1
    if File.directory?(args[0])
      if File.directory?("#{args[0]}/manager")
        args = [File.realpath("#{args[0]}/manager")]
      end
      Dir.foreach(args[0]) do |sim|
        if sim =~ /^simulation-/ && File.directory?("#{args[0]}/#{sim}")
          args.push(File.realpath("#{args[0]}/#{sim}")) 
        end
      end
    end
  end
  
  opts[:files] = args
  return opts
end

opts = parse_options
print "Usage: #{$0} [-p|-h] FILES|DIRECTORIES...
To use, run muscle2 with the flags '-l plain', and run this script on the
output directory of muscle2. When doing a distributed execution, put the output
directories from all machines in the same local directory, and run on one of
these directories. The -p flag shows the complete output of a distributed simulation.\n" if opts[:help] || opts[:files].empty?

unless opts[:files].empty?
  data = read_logs(opts[:files])
  min_time = data[0][:time]
  print_logs(data, min_time) if opts[:print_logs]
  instances = organize(data)
  print_statistics(instances, min_time, data[data.size - 1][:time])
end
