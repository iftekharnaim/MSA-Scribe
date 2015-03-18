close all;
human_response = dlmread('../human_evaluation_docs/human_response_final.csv', '\t', 1, 0);
automated_scores = dlmread('../human_evaluation_docs/automated_results.csv',',');

human_response = human_response(1:25,:);

% for i=1:size(human_response,1)
%     
%     mini = min(human_response(i,1:8));
%     maxi = max(human_response(i,1:8));
%     range = maxi-mini;
%     human_response(i,1:8) = ((human_response(i,1:8)-mini)/range) * 10.0;
%     %
%     mini = min(human_response(i,9:16));
%     maxi = max(human_response(i,9:16));
%     range = maxi-mini;
%     human_response(i,9:16) = ((human_response(i,9:16)-mini)/range) * 10.0;   
% end

%%
wer = repmat(automated_scores(:,1)', size(human_response,1),1);
bleu = repmat(automated_scores(:,2)', size(human_response,1),1);
fmeasure = repmat(automated_scores(:,3)', size(human_response,1),1);

% indx = [1 2 4 5 6 8];
% 
% human_response = human_response(:,indx);
% wer = wer(:,indx);
% bleu= bleu(:,indx);
% fmeasure = fmeasure(:,indx);


%%
[Rwer,P,L,U] = corrcoef(human_response(:),1-wer(:));
[Rwer1,pwer1] = corr(human_response(:),1-wer(:),'type', 'Spearman');

[Rbleu,P,L,U] = corrcoef(human_response(:),bleu(:));
[Rbleu1,pbleu1] = corr(human_response(:),bleu(:), 'type', 'Spearman');

[Rf,P,L,U] = corrcoef(human_response(:),fmeasure(:));
[Rf1, pf1] = corr(human_response(:),fmeasure(:), 'type', 'Spearman');
%%
[Rwer3, pwer3] = corr(mean(human_response,1)',1-wer(1,:)','type', 'Pearson');

[Rbleu3,pbleu3] = corr(mean(human_response,1)',bleu(1,:)', 'type', 'Pearson');

[Rf3,pf3] = corr(mean(human_response,1)',fmeasure(1,:)', 'type', 'Pearson');
%%
[Rwer2, pwer2] = corr(mean(human_response,1)',1-wer(1,:)','type', 'Spearman');

[Rbleu2,pbleu2] = corr(mean(human_response,1)',bleu(1,:)', 'type', 'Spearman');

[Rf2,pf2] = corr(mean(human_response,1)',fmeasure(1,:)', 'type', 'Spearman');
%%
disp('Pearson');
disp([Rwer3 Rbleu3 Rf3]);

disp('Spearman');
disp([Rwer2 Rbleu2 Rf2]);

%%

%%
[Rwer4, pwer4] = corr(median(human_response,1)',1-wer(1,:)','type', 'Pearson');

[Rbleu4,pbleu4] = corr(median(human_response,1)',bleu(1,:)', 'type', 'Pearson');

[Rf4,pf4] = corr(median(human_response,1)',fmeasure(1,:)', 'type', 'Pearson');
%%
[Rwer5, pwer5] = corr(median(human_response,1)',1-wer(1,:)','type', 'Spearman');

[Rbleu5,pbleu5] = corr(median(human_response,1)',bleu(1,:)', 'type', 'Spearman');

[Rf5,pf5] = corr(median(human_response,1)',fmeasure(1,:)', 'type', 'Spearman');

disp('Pearson');
disp([Rwer4 Rbleu4 Rf4]);

disp('Spearman');
disp([Rwer5 Rbleu5 Rf5]);


%%
figure;
for i=1:16
    hold on;
    scatter(1-wer(:,i),human_response(:,i));
end


figure;
scatter(median(1-wer,1), mean(human_response,1), 'Linewidth',2);

figure;
scatter(median(bleu,1), mean(human_response,1), 'Linewidth',2);

figure;
scatter(median(1-wer,1), mean(human_response,1), 'Linewidth',2);

%%
%regress(mean(human_response,1),median(1-wer,1));
