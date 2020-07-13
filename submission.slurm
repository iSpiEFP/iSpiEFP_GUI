#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/test1.stdout
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=10000
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/test1.in > iSpiClient/Libefp/output/test1.out
