#ifndef __CMuscle_Topology_Hpp
#define __CMuscle_Topology_Hpp

#include "endpoint.h"
#include "exception.hpp"

#include <sstream>
#include <map>
#include <vector>

namespace muscle {
	namespace util {
		std::vector<std::string> split(const std::string& str, const std::string& chars);
		std::string to_upper_ascii(const std::string& str);
		
		struct option_parser
		{
			struct value {
				std::string name;
				std::string description;
				bool hasArg;
				value(std::string name, std::string description, bool hasArg = true) : name(name), description(description), hasArg(hasArg) {}
			};
			
			std::vector<value> vals;
			
			void add(std::string name, std::string description, bool hasArg = true)
			{
				vals.push_back(value(name, description, hasArg));
			}
			
			bool has(std::string str)
			{
				return results.find(str) != results.end();
			}
			
			template<typename T>
			inline T getImpl(std::map<std::string,std::string>::iterator it)
			{
				T result;
				std::stringstream ss(it->second);
				ss >> result;
				return result;
			}
			
			template <typename T> T get(std::string name, T def);
			template <typename T> T forceGet(std::string name);
			
			std::map<std::string,std::string> results;
			bool load(std::string fname);
			bool load(int argc, char **argv);
			void print();
		};
		
		template<>
		inline std::string option_parser::getImpl<std::string>(std::map<std::string,std::string>::iterator it)
		{
			return it->second;
		}
		
		template<typename T>
		T option_parser::get(std::string name, T def)
		{
			std::map<std::string,std::string>::iterator it = results.find(name);
			if (it == results.end())
				return def;
			
			return getImpl<T>(it);
		}
		
		template<typename T>
		T option_parser::forceGet(std::string name)
		{
			std::map<std::string,std::string>::iterator it = results.find(name);
			if (it == results.end())
				throw muscle::muscle_exception("Required option '" + name + "' is not set", 0, true);
			
			return getImpl<T>(it);
		}
		
		bool loadTopology(std::string fname, std::map<std::string,muscle::net::endpoint> & results);
	}
}


#endif

