program sender
  	implicit none
	double precision :: allData(1:65536)
	integer :: i, sz
	logical(4) :: wstop, hasProp
	character(len=255) :: propName, kernelName, prop, portName
	enum, bind(c)
		enumerator :: MUSCLE_DOUBLE, MUSCLE_FLOAT, MUSCLE_INT32, MUSCLE_INT64, MUSCLE_STRING, MUSCLE_BOOLEAN, MUSCLE_RAW
	endenum
	
	call muscle_fortran_init()
	
	call MUSCLE_Kernel_Name(kernelName)
	write (*,*) kernelName
	
	propName = "dt"//char(0)
	call MUSCLE_Has_Property(propName, hasProp)
	write (*,*) hasProp
	if (hasProp) then
		call MUSCLE_Get_Property(propName, prop)
		write (*,*) prop
	end if
	
	do i = 1, 65536
		allData(i) = i
	end do
	
	portName = "data"//char(0)
	sz = 65536
	call MUSCLE_Will_Stop(wstop)
	do while (.NOT. wstop)
		call MUSCLE_Send(portName, allData(1:sz), sz, MUSCLE_DOUBLE)
		call MUSCLE_Will_Stop(wstop)
	end do
	
    call MUSCLE_Finalize()
end program sender

subroutine muscle_fortran_init()
	implicit none
	integer :: argc, i, prevlen, newlen
    character(len=25600) :: argv
    character(len=255) :: arg

    prevlen = 0
	argc = command_argument_count()
	
	do i = 0, argc
        call get_command_argument(i, arg)
		newlen = len_trim(arg)
        argv = argv(1:prevlen) // arg(1:newlen) // char(0)
		prevlen = prevlen + newlen + 1
    end do
    
    call MUSCLE_Init(argc, argv(1:prevlen))
end subroutine muscle_fortran_init
