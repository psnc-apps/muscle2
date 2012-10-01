program sender
  	implicit none
	double precision :: allData(1:65536)
	integer :: i
	logical(4) :: wstop
	enum, bind(c)
		enumerator :: MUSCLE_DOUBLE, MUSCLE_FLOAT, MUSCLE_RAW, MUSCLE_INT32, MUSCLE_INT64, MUSCLE_BOOLEAN, MUSCLE_STRING
	endenum
	
	call muscle_fortran_init()
	
	do i = 1, 65536
		allData(i) = i
	end do
	
	call MUSCLE_Will_Stop(wstop)
	do while (.NOT. wstop)
		call MUSCLE_Send('data'//char(0),allData,%REF(65536),%REF(MUSCLE_DOUBLE))
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
