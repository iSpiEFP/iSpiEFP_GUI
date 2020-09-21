#!/bin/csh
#SBATCH -o iSpiClient/Gamess/jobs/testpw/output/testpw.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/gamess/gamess/rungms iSpiClient/Gamess/jobs/testpw/input/testpw.inp > iSpiClient/Gamess/jobs/testpw/output/testpw.out
