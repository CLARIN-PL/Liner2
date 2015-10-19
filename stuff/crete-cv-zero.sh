for FOLD_NUM in {0..9};
do
    echo Training model for fold $FOLD_NUM ...
    # train_models
    ./crete-cli train -m $1/config/crete-zero.ini -i batch:ccl_rel -f $1/fold_train_${FOLD_NUM}.txt -c $1/models/zero_${FOLD_NUM}.mdl;

    echo Testing model for fold $FOLD_NUM ...
    # classify fold
    ./crete-cli classify -m $1/config/crete-zero.ini -i batch:ccl -f $1/fold_compare_${FOLD_NUM}.txt -o batch:ccl_rel -t $1/results_zero/fold_test_${FOLD_NUM}.txt -c $1/models/zero_${FOLD_NUM}.mdl;
done


