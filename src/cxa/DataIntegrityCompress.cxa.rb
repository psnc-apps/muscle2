abort "Run 'source [MUSCLE_HOME]/etc/muscle.profile' before this script" if not ENV.has_key?('MUSCLE_HOME')
dir = ENV['MUSCLE_HOME'] + '/share/muscle/examples/dataintegrity'

# declare kernels
bounce = NativeInstance.new('Bounce', "#{dir}/bounce")
check = NativeInstance.new('Check', "#{dir}/check", java_class: 'examples.dataintegrity.Check')

# configure connection scheme
check.couple(bounce, 'datatype')
# 3.93s user 0.55s system 79% cpu 5.617 total
check.couple(bounce, {'out' => 'in'}, ['serialize','chunk_4','compress','thread'], ['decompress', 'dechunk_4', 'deserialize'])
bounce.couple(check, {'out' => 'in'}, ['serialize','chunk_4','compress','thread'], ['decompress', 'dechunk_4', 'deserialize'])

# configure cxa properties
$env['max_timesteps'] = 1;
$env['default_dt'] = 1
check['num_seeds'] = 1

# 4.71s user 0.66s system 81% cpu 6.566 total
#tie('out', 'in',['serialize','chunk_32','thread','compress'],['decompress', 'dechunk_32', 'deserialize'])
# 4.71s user 0.63s system 80% cpu 6.602 total 
#tie('out', 'in',['serialize','chunk_32','compress'],['decompress', 'dechunk_32', 'deserialize'])
# 4.00s user 0.55s system 79% cpu 5.708 total
#tie('out', 'in',['serialize','chunk_4','thread','compress'],['decompress', 'dechunk_4', 'deserialize'])
# 3.98s user 0.55s system 80% cpu 5.641 total
#tie('out', 'in',['serialize','compress'],['decompress', 'deserialize'])

