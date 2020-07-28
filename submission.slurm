#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/Title.stdout
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/Title.in > iSpiClient/Libefp/output/Title.out
