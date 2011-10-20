/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

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
*/

#ifndef JNIArray_31EC43C5_23FB_4763_8D63_C63E7825994B
#define JNIArray_31EC43C5_23FB_4763_8D63_C63E7825994B

#include <jni.h> 
#include <string>

#include <ArrayTraits.h>
#include <JNITool.h>

namespace muscle {

/**
provides simplified access to a jarray
\author Jan Hegewald
*/
template<typename T, typename Traits = ArrayTraits<T> >
class JNIArray
{
public:
	typedef typename Traits::ARRAYTYPE ARRAYTYPE;
	typedef typename Traits::PTYPE PTYPE;
private:
	typedef std::size_t size_type;

//
public:

	JNIArray(JNIEnv* newEnv, jsize length)
		: env(newEnv), dataJref(NULL), dataBody(NULL)
	{
		assert(env != NULL);
		// alloc a new jarray
		ARRAYTYPE newDataJRef = Traits::NewArray(env, length);
		setData(newDataJRef);
	}

	//
	JNIArray(JNIEnv* newEnv, ARRAYTYPE newDataJRef)
		: env(newEnv), dataJref(NULL), dataBody(NULL)
	{
		setData(newDataJRef);
	}


// disallow copy constructor and assignment
// because we would need a custom implementation
private:
	JNIArray(const JNIArray<PTYPE>& other)
		: env(NULL), dataJref(NULL), dataBody(NULL)
	{
	}
	JNIArray<PTYPE>& operator=(const JNIArray<PTYPE>& other)
	{
		return other;// never use this, only here to make the compiler happy
	}
public:	
	
	~JNIArray()
	{
		release();
	}
	
	
	const jsize size()
	{
		return env->GetArrayLength(dataJref);
	}
	const jsize size() const
	{
		return env->GetArrayLength(dataJref);
	}
	
	
//	PTYPE* data()
//	{
//		assert(dataBody != NULL);
//		return dataBody;
//	}
	

   PTYPE& operator[](const size_type& i)
   {
		assert(dataBody != NULL);
      return dataBody[i];
   }
   const PTYPE& operator[](const size_type& i) const
   {
		assert(dataBody != NULL);
      return dataBody[i];
   }

	/**
	sync content with java data
	*/
	void commit()
	{
		if(dataJref != NULL && dataBody != NULL)
		{
			jint mode = JNI_COMMIT; // copy back the content to dataJref (if necessary) but do not free the dataBody
			Traits::ReleaseArrayElements(env, dataJref, dataBody, mode);
		}
	}
	
	
	void setRegion(jsize& start, jsize& elementCount, PTYPE*& buf)
	{
		if(start+elementCount > size())
		{
			std::stringstream exceptionMessage;
			exceptionMessage << __FILE__ << ":" << __LINE__ << "start+elementCount ("<<start<<"+"<<elementCount<<") > size ("<<size()<<")";
			throw std::runtime_error(exceptionMessage.str());
		}
		
		//Traits::SetArrayRegion(env, dataJref, start, len, buf); // this only sets the data of the java array, which might be a copy
		for(int i = start; i < start+elementCount; i++)
			dataBody[i] = buf[i];
	}
//	void setRegion(jsize& start, jsize& len, const PTYPE*& buf)
//	{
//		Traits::SetArrayRegion(env, dataJref, start, len, buf);
//	}


//
private:
	// call this from constructor
	void setData(ARRAYTYPE newDataJRef)
	{
		release();
		dataJref = newDataJRef;
		// GetArrayElements
		// get a pointer to a primitive array inside the JVM
		jboolean isCopy;
		dataBody = Traits::GetArrayElements(env, dataJref, &isCopy); // do not forget to release this!
		JNITool::catchJREException(env, __FILE__, __LINE__);
	}
	
	void release()
	{
		// ReleaseArrayElements
		if(dataBody != NULL)
		{
			// release our hold on javas array
			// (this tells the java garbage collector we no longer need the tmpdata)
			jint mode = 0; // copy back the content and free the elems buffer
			//if(isCopy == JNI_TRUE)
			//	mode = JNI_ABORT; // free the buffer without copying back the possible changes
			Traits::ReleaseArrayElements(env, dataJref, dataBody, mode);
			JNITool::catchJREException(env, __FILE__, __LINE__);
			dataBody = NULL;
		}
		
		// free local reference to help the garbage collector, see section 5.2.1 jni.pdf
		if(dataJref != NULL)
		{
			env->DeleteLocalRef(dataJref);
			dataJref = NULL;
		}
	}

	
//
private:
	JNIEnv* env;
	ARRAYTYPE dataJref;
	PTYPE* dataBody;
};


} // EO namespace muscle
#endif
