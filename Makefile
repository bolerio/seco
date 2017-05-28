CWD = $(CURDIR)

all: docs/SECO_Developer.pdf
#tmp/rse.pdf

LATEX = pdflatex -interaction=nonstopmode -output-directory=$(CWD)/tmp
docs/%.pdf: tmp/%.pdf
	mv $< $@
tmp/%.pdf: docs/%.tex Makefile
	$(LATEX) $< 

docs/rse.tex: ./sigplanconf.cls
./sigplanconf.cls:
	wget -O $@ http://www.sigplan.org/sites/default/files/sigplanconf.cls
