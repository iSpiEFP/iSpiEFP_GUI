#!/bin/csh
#SBATCH -o iSpiClient/Libefp/output/ethhhhannnol.stdout
#SBATCH -A lslipche
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=1000
/depot/lslipche/apps/iSpiEFP/packages/libefp/bin/efpmd iSpiClient/Libefp/input/ethhhhannnol.in > iSpiClient/Libefp/output/ethhhhannnol.out
