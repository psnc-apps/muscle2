#include "option_parser.hpp"
#include "../../muscle2/logger.hpp"

#include <stdint.h>
#include <cstring>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <set>
#include <getopt.h>

#ifndef UINT16_MAX
#define UINT16_MAX 65535
#endif

using namespace std;
using namespace muscle;

bool option_parser::load(int argc, char **argv)
{
    int c;

    char * const optstr = new char[vals.size()*2+1];
    char *opt_ptr = optstr;
    vector<value>::const_iterator it = vals.begin();
    static struct option *long_options = new struct option[vals.size()+1];
    for (size_t i = 0; i < vals.size(); ++it, ++i) {
        long_options[i].name = it->name.c_str();
        long_options[i].flag = 0;
        long_options[i].has_arg = it->hasArg;
        long_options[i].val = *opt_ptr = (char)(i + 'a');
        ++opt_ptr;
        if (it->hasArg)
        {
            *opt_ptr = ':';
            ++opt_ptr;
        }
    }
    memset(&long_options[vals.size()], 0, sizeof(struct option));
    *opt_ptr = '\0';
    
    while (true)
    {
        /* getopt_long stores the option index here. */
        int option_index = 0;
        
        c = getopt_long(argc, (char **)argv, optstr,
                         long_options, &option_index);
        
        /* getopt_long already printed an error message. */
        if (c == '?')
            continue;
        /* Detect the end of the options. */
        if (c == -1)
            break;
        
        c -= 'a';
        
        // Argv parameters take highest precedence, they will overwrite everything
        if (vals[c].hasArg)
            results[vals[c].name] = string(optarg);
        else
            results[vals[c].name] = string(""); // empty constructor
    }
    
	delete [] optstr;
	
    // Parsed all arguments
    return (optind == argc);
}

void option_parser::print()
{
    int maxlen = 0;
    for (vector<value>::const_iterator it = vals.begin(); it != vals.end(); ++it)
    {
        int total = int(it->name.length() + (it->hasArg ? 4 : 0));
        if (total > maxlen)
            maxlen = total;
    }
    cout << "Options:" << endl;
    for (vector<value>::const_iterator it = vals.begin(); it != vals.end(); ++it)
    {
        
        cout << "  --" << it->name;
        if (it->hasArg)
            cout << " arg";
        
        int total = 2 + maxlen - int(it->name.length() + (it->hasArg ? 4 : 0));
        for (int i = 0; i < total; i++)
            cout << " ";
        
        cout << it->description << endl;
    }
}

bool option_parser::load(string fname)
{
    ifstream file(fname.c_str());
    if(!file)
        return false;
    
    string line;
    const string commentS("//");
    const string commentH("#");
    // also include equals signs
    const string spaces(" \n\r\t=");
    while(file.good())
    {
        std::getline(file, line);
        size_t ptr = line.find_first_not_of(spaces);
        
        if (ptr == line.npos || line.compare(ptr, 1, commentH) == 0 || line.compare(ptr, 2, commentS) == 0)
            continue;
        
        vector<string> match = split(line, spaces);
        
        if (match.size() == 1)
            results.insert(pair<string, string>(match[0], ""));
        else if (match.size() == 2)
            results.insert(pair<string, string>(match[0], match[1]));
        else
            throw muscle::muscle_exception("Syntax error in config file, on line: '" + line + "'");
        
        // File parameters take lowest precedence, they will not overwrite anything
    }
    
    return true;
}

bool loadTopology(string fname, map<string, muscle::endpoint> & results)
{
	if (fname.empty())
		return false;
	
    ifstream file(fname.c_str());
    if(!file)
    {
        logger::severe("Opening topology file (%s) failed", fname.c_str());
        return false;
    }
    
    string line;
    const string commentS("//");
    const string commentH("#");
    const string spaces(" \n\r\t");
    
    while(file.good())
    {
        std::getline(file, line);
        size_t ptr = line.find_first_not_of(spaces);
        
        if (ptr == line.npos || line.compare(ptr, 1, commentH) == 0 || line.compare(ptr, 2, commentS) == 0)
            continue;
        
        vector<string> match = split(line, spaces);
        if (match.size() != 2 && match.size() != 3) {
            throw muscle::muscle_exception("Syntax error in config file, on line: '" + line + "'");
        }
        
        if(results.find(match[0])!=results.end())
        {
            logger::severe("Redefinition of machine  %s", match[0].c_str());
            return false;
        }
        
        char *endP;
        uint16_t port = 0;
        if (match.size() == 3) {
            long portl = strtol(match[2].c_str(), &endP, 10);
            if (endP == match[2].c_str() || portl > UINT16_MAX)
            {
                logger::severe("Syntax error in config file, in line: '%s'", line.c_str());
                return false;
            }
            port = (uint16_t)portl;
        }
        
        muscle::endpoint& ep = results[match[0]] = muscle::endpoint(match[1], port);

        stringstream ss;
        ss << "Adding machine " << match[0] << " as " << ep.getHost();

        if(port == 0)
            ss << " without open port";
        else
            ss << ":" << match[2];
        
        logger::config(ss.str().c_str());
    }
    
    return true;
}

vector<string> split(const string& str, const string& chars)
{
    vector<string> list;
    size_t ptr = str.find_first_not_of(chars);
    size_t old_ptr = ptr;
    while (ptr != str.npos) {
        ptr = str.find_first_of(chars, ptr);
        list.push_back(str.substr(old_ptr, ptr - old_ptr));
        if (ptr != str.npos)
        {
            ptr = str.find_first_not_of(chars, ptr);
            old_ptr = ptr;
        }
    }
    
    return list;
}

string to_upper_ascii(const string& str)
{
    char *newstr = new char[str.length()+1];
    char *ptr = newstr;
    const char *oldstr = str.c_str();
    
    for (int i = 0; i < str.length(); i++)
    {
        if (*oldstr >= 'a' && *oldstr <= 'z')
            *ptr++ = *oldstr++ - 'a' + 'A';
        else
            *ptr++ = *oldstr++;
    }
    *ptr = '\0';
    string news(newstr);
    delete [] newstr;
    return news;
}

//RTRIM
//s.erase(s.find_last_not_of(" \n\r\t")+1);
