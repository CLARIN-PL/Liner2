~/projects/Liner2/liner2-cli train -m /home/kotu/Downloads/2018-05-15-kpwr-event/model_modality_train/cfg.ini
~/projects/Liner2/liner2-cli train -m /home/kotu/Downloads/2018-05-15-kpwr-event/model_generality_train/cfg.ini -v
~/projects/Liner2/liner2-cli train -m /home/kotu/Downloads/2018-05-15-kpwr-event/model_polarity_train/cfg.ini -v
~/projects/Liner2/liner2-cli train-rel -m /home/kotu/Downloads/2018-05-15-kpwr-event/model_event_relations/cfg.ini --mode train -f /home/kotu/Downloads/2018-05-15-kpwr-event/2018-05-15-kpwr-event_all/train.txt -t /home/kotu/Downloads/2018-05-15-kpwr-event/model_event_relations/eventrelations -v --relations alink,slink,null --content
#test last model
~/projects/Liner2/liner2-cli train-rel  -m /home/kotu/Downloads/2018-05-15-kpwr-event/2018-05-15-kpwr-event_all/cfg.ini --mode test -f /home/kotu/Downloads/2018-05-15-kpwr-event/2018-05-15-kpwr-event_all/test.txt -t /home/kotu/Downloads/2018-05-15-kpwr-event/model_event_relations/eventrelations -v --relations slink,alink
