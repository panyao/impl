def start_test_processes

  @serv_pid = fork { `java -cp dropbox.jar org.cs27x.dropbox.Dropbox -dir #{@currentDir}/#{@serverDir}/ >> server.log` }
  sleep 12
  @client_pid = fork { `java -cp dropbox.jar org.cs27x.dropbox.Dropbox -dir #{@currentDir}/#{@clientDir}/ -connectTo #{@clientConnectTo} >> client.log` }

end

def test_file_write
  @testFilePath = "#{@currentDir}/#{@serverDir}/test"
  if File.exist? @testFilePath then File.delete @testFilePath end
  @testFile = File.open(@testFilePath, 'a+')
  @testFile << "this is test text"
  servHash = `md5 #{@currentDir}/#{@serverDir}/test`.split(' ')[-1]
  sleep 10
  clientHash = `md5 #{@currentDir}/#{@clientDir}/test`.split(' ')[-1]
  puts "Do the files have the same hash?  : #{servHash == clientHash}"
  puts "Serv: #{servHash}  Client: #{clientHash}"
end

def test_file_delete
  File.delete @testFilePath
  sleep 20
  clientFilePath = "#{@currentDir}/#{@clientDir}/test"
  puts "Does the deleted file exist?  :  #{File.exists? clientFilePath}"
end

if $0 == __FILE__
  @currentDir = Dir.pwd
  @serverDir = ARGV[0]
  @clientDir = ARGV[1]
  @clientConnectTo = ARGV[2]
  start_test_processes
  puts "############### PROCESS IDS :  #{@serv_pid}  : #{@client_pid} ##################"
  sleep 15
  test_file_write
  test_file_delete
  Process.kill("INT", @serv_pid)
  Process.kill("INT", @client_pid)
  `killall java`
end
