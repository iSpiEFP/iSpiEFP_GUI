#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/2b6w.stdout
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/2b6w.in > iSpiClient/Libefp/output/2b6w.out
