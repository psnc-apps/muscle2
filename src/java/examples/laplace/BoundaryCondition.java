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

package examples.laplace;

/**
interface for boundary conditions
@author Jan Hegewald
*/
public interface BoundaryCondition {

   public boolean applies(int x, int y);
   public double get(int x, int y, int step);

   /**
	boundary condition which applies to the main area of the grid<br>
	calculates new value bases on the four neighbour values
	*/
   public static class AreaCondition implements BoundaryCondition {

		int nx;
		int ny;
		BoundaryCondition[] otherConditions;
		double[][] data;

		public AreaCondition(int newNx, int newNy, double[][] newData, BoundaryCondition ... newOtherConditions) {

			this.nx = newNx;
			this.ny = newNy;
			this.otherConditions = newOtherConditions;
			this.data = newData;
		}

      public boolean applies(int x, int y) {

			for (BoundaryCondition otherCondition : this.otherConditions) {
				if(otherCondition.applies(x, y)) {
					return false;
				}
			}

			return true;
		}

      // calc new value based on the neighbours
      public double get(int x, int y, int step) {

         double n = this.data[x][y+1];
         double e = this.data[x+1][y];
         double s = this.data[x][y-1];
         double w = this.data[x-1][y];

         return (n+e+s+w)/4.0;
      }
   }


   //
	public static class NorthBoundary implements BoundaryCondition {

		int nx;
		int ny;

		public NorthBoundary(int newNx, int newNy, double[][] newData) {

			this.nx = newNx;
			this.ny = newNy;
		}

      public boolean applies(int x, int y) {

			return y == this.ny-1;
		}

      public double get(int x, int y, int step) {

         return Math.sin(x * 2 * Math.PI / this.nx);
      }
   }


	//
   public static class EastBoundary implements BoundaryCondition {

		int nx;
		int ny;

		public EastBoundary(int newNx, int newNy, double[][] newData) {

			this.nx = newNx;
			this.ny = newNy;
		}

      public boolean applies(int x, int y) {

			return x == this.nx-1;
		}

      public double get(int x, int y, int step) {

			return 0D;
		}
   }


	//
   public static class SouthBoundary implements BoundaryCondition {

		int nx;
		int ny;

		public SouthBoundary(int newNx, int newNy, double[][] newData) {

			this.nx = newNx;
			this.ny = newNy;
		}

      public boolean applies(int x, int y) {

			return y == 0;
		}

      public double get(int x, int y, int step) {

         return Math.cos(x * 2 * Math.PI / this.nx + Math.PI);
      }
   }


	//
   public static class WestBoundary implements BoundaryCondition {

		int nx;
		int ny;

		public WestBoundary(int newNx, int newNy, double[][] newData) {

			this.nx = newNx;
			this.ny = newNy;
		}

      public boolean applies(int x, int y) {

			return x == 0;
		}

      public double get(int x, int y, int step) {

         return 0D;
      }
   }


	/**
	this condition will be applied to initialize a newly created domain
	*/
   public static class DefaultCondition implements BoundaryCondition {

		int nx;
		int ny;

		public DefaultCondition(int newNx, int newNy, double[][] newData) {

			this.nx = newNx;
			this.ny = newNy;
		}

      public boolean applies(int x, int y) {

			return true;
		}

      public double get(int x, int y, int step) {

         return 0D;
      }
   }

}
