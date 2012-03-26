#include <iostream>
#include <vector>
#include <map>

using namespace std;

struct mto_config
{
  string address;
  string port;
};

bool loadTopology(const char * fname, map<string, mto_config> & results);

