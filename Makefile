CWD = $(CURDIR)

all: 
#tmp/rse.pdf

LATEX = pdflatex -interaction=nonstopmode -output-directory=$(CWD)/tmp
tmp/%.pdf: docs/%.tex Makefile
	$(LATEX) $< && touch $@ 

docs/rse.tex: ./sigplanconf.cls
./sigplanconf.cls:
	wget -O $@ http://www.sigplan.org/sites/default/files/sigplanconf.cls
