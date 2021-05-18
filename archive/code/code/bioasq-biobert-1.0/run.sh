export BIO_ASQM_DIR=/home/sai/dl_txt/biobert/BERT-pubmed-1000000-SQuAD
export BIO_ASQ_DIR=/home/sai/dl_txt/biobert/CORD19
#export BIO_ASQ_DIR=/home/sai/dl_txt/biobert/BioASQ-67b-15Oct2019/BioASQ-6b/test/Full-Abstract

python run_factoid.py      --do_train=False      --do_predict=True      --vocab_file=$BIO_ASQM_DIR/vocab.txt      --bert_config_file=$BIO_ASQM_DIR/bert_config.json      --init_checkpoint=$BIO_ASQM_DIR/model.ckpt-14599      --max_seq_length=384      --train_batch_size=12      --learning_rate=5e-6      --doc_stride=128      --num_train_epochs=5.0      --do_lower_case=False      --predict_file=$BIO_ASQ_DIR/novel_th_ab_bert.json      --output_dir=/tmp/factoid/
