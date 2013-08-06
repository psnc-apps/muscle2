//
//  assertTemplates.h
//  CMuscle
//
//  Created by Joris Borgdorff on 05-08-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef CMuscle_assertTemplates_h
#define CMuscle_assertTemplates_h

#include <iostream>
#include <cstring>
#include <cstdlib>
#include <sstream>
#include <vector>

using namespace std;

int failed = 0, total = 0;

void assert(bool passed, string msg)
{
    total++;
    if (passed)
    {
        cout << "[ OK     ] " << msg << endl;
    }
    else
    {
        cout << "[ FAILED ] " << msg << endl;
        failed++;
    }
}

template <typename T>
void assertEquals(T received, T expected, string msg)
{
    bool ok = (received == expected);
    if (!ok)
    {
        stringstream ss;
        ss << msg << " (" << expected << " expected but " << received << " given)";
        msg = ss.str();
    }
    assert(ok, msg);
}
template <>
void assertEquals<const char *>(const char *received, const char *expected, string msg)
{
    bool ok = strcmp(received, expected) == 0;
    if (!ok)
    {
        stringstream ss;
        ss << "(" << expected << " expected but " << received << " given) " << msg;
        msg = ss.str();
    }
    assert(ok, msg);
}
template <typename T>
ostream& vectorStr(ostream& os, const vector<T>& v)
{
    typename vector<T>::const_iterator it = v.begin();
    os << "[";
    if (it != v.end())
        os << *it;
    ++it;
    
    for (; it != v.end(); ++it)
    {
        os << ", " << *it;
    }
    os << "]";
    return os;
}

template <>
void assertEquals<vector<string> >(vector<string> received, vector<string> expected, string msg)
{
    bool ok = received == expected;
    if (!ok)
    {
        stringstream ss;
        ss << "(";
        vectorStr(ss, expected) << " expected but ";
        vectorStr(ss, received) << " given) " << msg;
        msg = ss.str();
    }
    assert(ok, msg);
}
void assertFalse(string msg) { assert(false, msg); }
void assertTrue(string msg) { assert(true, msg); }

#endif
