#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/complexTest.stdout
#SBATCH -A lslipche
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 4:00:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/complexTest.in > iSpiClient/Libefp/output/complexTest.out
