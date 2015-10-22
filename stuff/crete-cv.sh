for FOLD_NUM in {0..9}; 
do  
    # create index files
    ls $1/fold_$FOLD_NUM/train/*[^rel].xml > $1/fold_$FOLD_NUM/train/index_all.txt;
    ls $1/fold_$FOLD_NUM/test/*[^rel].xml > $1/fold_$FOLD_NUM/test/index_all.txt;
    ls $1/fold_$FOLD_NUM/compare/*[^rel].xml > $1/fold_$FOLD_NUM/compare/index_all.txt;
    
    # train_models
    ./crete-cli train -m $1/fold_$FOLD_NUM/crete/crete-named.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/train/index_all.txt;
    ./crete-cli train -m $1/fold_$FOLD_NUM/crete/crete-agp.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/train/index_all.txt;
    ./crete-cli train -m $1/fold_$FOLD_NUM/crete/crete-pron.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/train/index_all.txt;
    ./crete-cli train -m $1/fold_$FOLD_NUM/crete/crete-zero.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/train/index_all.txt;
    # classify fold
    ./crete-cli classify -m $1/fold_$FOLD_NUM/crete/crete-named.ini -i batch:ccl -f $1/fold_$FOLD_NUM/test/index_all.txt -o batch:ccl_rel -t $1/fold_$FOLD_NUM/test/index_all.txt;
    ./crete-cli classify -m $1/fold_$FOLD_NUM/crete/crete-agp.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/test/index_all.txt -o batch:ccl_rel -t $1/fold_$FOLD_NUM/test/index_all.txt;
    ./crete-cli classify -m $1/fold_$FOLD_NUM/crete/crete-pron.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/test/index_all.txt -o batch:ccl_rel -t $1/fold_$FOLD_NUM/test/index_all.txt;
    ./crete-cli classify -m $1/fold_$FOLD_NUM/crete/crete-zero.ini -i batch:ccl_rel -f $1/fold_$FOLD_NUM/test/index_all.txt -o batch:ccl_rel -t $1/fold_$FOLD_NUM/test/index_all.txt;
done
