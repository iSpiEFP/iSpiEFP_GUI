#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/complexOPT.stdout
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/complexOPT.in > iSpiClient/Libefp/output/complexOPT.out
