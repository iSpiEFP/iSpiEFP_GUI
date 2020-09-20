#!/bin/csh
#SBATCH -o iSpiClient/Gamess/jobs/test/output/test.err
#SBATCH -A lslipche
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/gamess/gamess/rungms iSpiClient/Gamess/jobs/test/input/test.inp > iSpiClient/Gamess/jobs/test/output/test.out
