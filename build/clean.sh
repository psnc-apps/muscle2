find . -maxdepth 1 ! \( -name . -o -name build.sh -o -name clean.sh -o -name compat -o -name old \) -exec rm -rf {} \;
