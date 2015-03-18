clear all;close all;clc;
%% read the data
list_bleu_thresh = [];
list_fmeasure_thresh = [];
list_wer_thresh = [];

list_bleu_nothresh = [];
list_fmeasure_nothresh = [];
list_wer_nothresh = [];

list_bleu_graph = [];
list_fmeasure_graph = [];
list_wer_graph = [];

%
weight = 3;
%
array{1} = 'e6p11';
array{2} = 'e6p22';
array{3} = 'sch1p11';
array{4} = 'sch1p22';

chunk_sizes =[5 7 10 15 20 30 40 60];
weights = [1.7 1.75 1.8 2 2.5 3 4 6 8];

%%
list_muscle_wer = [0.45638629283489096 0.5888324873096447 0.5805243445692884 0.6956521739130435];
list_muscle_bleu = [0.3681432605597549 0.18786362160006267 0.23290802541939046 0.0937611704730668];
list_muscle_fmeasure = [0.5525197906002253 0.4449385402284535 0.3875001226472366 0.34956805003034286];
%%
%list_fmeasure_graph =[ 0.5062    0.4949    0.4233    0.4235];
list_fmeasure_graph =[ 0.4660    0.4536    0.4259    0.3846];
%list_bleu_graph = [0.4055    0.4003    0.3193    0.3220]; % for 7 workers
list_bleu_graph = [0.3923    0.3891    0.3471    0.2889]; % for 10 workers
%list_wer_graph = [0.4673    0.4941    0.5406    0.6027]; %for 7
list_wer_graph = [0.5327    0.5059    0.5518    0.6373];%for 10


for i=1:4

    data_name = array{i};
    
    for j = 1:length(chunk_sizes)
        
        for k = 1:length(weights)
            
           %filename = ['./results_affine_same/output_' data_name '_' int2str(chunk_sizes(j)) '_' num2str(weights(k)) '_0_360.txt'];
           filename = ['./results_affine_same_closed/output_' data_name '_' int2str(chunk_sizes(j)) '_' num2str(weights(k)) '_0_360.txt'];
           
           disp(filename);
           
           data = [];
           data = textread(filename, '%s', 'bufsize', 50000);
           
           time = get_colon_separated_value(data{end});
           %wer_graph = get_colon_separated_value(data{end-5});            
           wer_thresh = get_colon_separated_value(data{end-8});           
           wer_nothresh = get_colon_separated_value(data{end-11});
           
           %f_graph = get_colon_separated_value(data{end-13});            
           f_thresh = get_colon_separated_value(data{end-19});           
           f_nothresh = get_colon_separated_value(data{end-22});
           
           %bleu_graph = get_colon_separated_value(data{end-30});            
           bleu_thresh = get_colon_separated_value(data{end-33});           
           bleu_nothresh = get_colon_separated_value(data{end-36});           
           
           %list_bleu_graph(i) = bleu_graph;
           %list_fmeasure_graph(i) = f_graph;
           %list_wer_graph(i) = wer_graph;
           
           list_bleu_nothresh(i,j,k) = bleu_nothresh;
           list_fmeasure_nothresh(i,j,k) = f_nothresh;           
           list_wer_nothresh(i,j,k) = wer_nothresh;
           
           list_bleu_thresh(i,j,k) = bleu_thresh;
           list_fmeasure_thresh(i,j,k) = f_thresh;           
           list_wer_thresh(i,j,k) = wer_thresh;    
           
           list_time(i,j,k) = get_colon_separated_value(data{end});            
           
           clear data;
        end
           
    end
end
%%
%% WER
colors = [1 0 0; 0 1 0; 0 0 1; 1 0 1; 0 1 1; 0.6 0.2 0;0 0 0;0.4 0.4 0.4];
marker = ['d' 'v' '*' 'o' '>' 's' '<', 's'];
%% Single curves 
legendnames = {'A^*-10-t';'A^*-15-t'; 'A^*-15'; 'Graph';'Muscle'};
for i = 1:4
    
    msa_2_10_wer = 1-list_wer_thresh(i,3,4);
    msa_2_15_wer = 1-list_wer_thresh(i,4,4);    
    msa_2_15_wer_nothresh = 1-list_wer_nothresh(i,4,4);
    
    data = [msa_2_10_wer; msa_2_15_wer; msa_2_15_wer_nothresh; 1-list_wer_graph(i); 1-list_muscle_wer(i)];
    
    for j=1:length(data)
        temp = [];
        temp = [num2str(data(j)*100, 4) '%'];
        legendnames2{j} = temp; 
    end
    
    figure;h = barweb(data, [0; 0; 0; 0; 0],0.6, [],'', '' ,'1-WER', 'Jet',[], legendnames,2, 'axis', legendnames2);
    set(gca, 'FontSize', 16);
    %title(num2str(i));
        ylim([0 0.65]);

    saveas(gcf, 'wer_benchmark.epsc');
    saveas(gcf, 'wer_benchmark.jpg');    
end
%%
%% estimate the average for each of the weights
%% BLEU
legendnames = {'A^*-10-t';'A^*-15-t'; 'A^*-15'; 'Graph';'Muscle'};
for i = 1:4
    
    msa_2_10_bleu = list_bleu_thresh(i,3,4);
    msa_2_15_bleu = list_bleu_thresh(i,4,4);    
    msa_2_15_bleu_nothresh = list_bleu_nothresh(i,4,4);
    
    data = [msa_2_10_bleu; msa_2_15_bleu; msa_2_15_bleu_nothresh; list_bleu_graph(i); list_muscle_bleu(i)];
    
    for j=1:length(data)
        temp = [];
        temp = [num2str(data(j)*100, 4) '%'];
        legendnames2{j} = temp; 
    end
    
    figure;h = barweb(data, [0; 0; 0; 0; 0],0.6, [],'', '' ,'BLEU', 'Jet',[], legendnames,2, 'axis', legendnames2);
    set(gca, 'FontSize', 16);
        ylim([0 0.65]);

    %title(num2str(i));
    saveas(gcf, 'bleu_benchmark.epsc');
    saveas(gcf, 'bleu_benchmark.jpg');    
end


%% FMEASURE
legendnames = {'A^*-10-t';'A^*-15-t'; 'A^*-15'; 'Graph';'Muscle'};
for i = 1:4
    
    msa_2_10_fmeasure = list_fmeasure_thresh(i,3,4);
    msa_2_15_fmeasure = list_fmeasure_thresh(i,4,4);    
    msa_2_15_fmeasure_nothresh = list_fmeasure_nothresh(i,4,4);
    
    data = [msa_2_10_fmeasure; msa_2_15_fmeasure; msa_2_15_fmeasure_nothresh; list_fmeasure_graph(i); list_muscle_fmeasure(i)];
    
    for j=1:length(data)
        temp = [];
        temp = [num2str(data(j)*100, 4) '%'];
        legendnames2{j} = temp; 
    end
    
    figure;h = barweb(data, [0; 0; 0; 0; 0],0.6, [],'', '' ,'F-measure', 'Jet',[], legendnames,2, 'axis', legendnames2);
    ylim([0 0.65]);
    set(gca, 'FontSize', 16);
    %title(num2str(i));
    saveas(gcf, ['fmeasure_benchmark' int2str(i) '.eps']);
    saveas(gcf, ['fmesaure_benchmark' int2str(i) '.jpg']);    
end
%% Get the percentage improvement
%diff_with_graph = (msa_2_15_wer_mean-graph_wer_mean)*100/graph_wer_mean;

diff_with_graph_wer = (list_wer_graph - list_wer_thresh(:,4,4)') ./(1-list_wer_graph);
mean(diff_with_graph_wer)
std(diff_with_graph_wer)

diff_with_graph_bleu = ( list_bleu_thresh(:,4,4)' - list_bleu_graph) ./(list_bleu_graph);
mean(diff_with_graph_bleu)
std(diff_with_graph_bleu)

diff_with_graph_fmeasure = ( list_fmeasure_thresh(:,4,4)' - list_fmeasure_graph) ./(list_fmeasure_graph);
mean(diff_with_graph_fmeasure)
std(diff_with_graph_fmeasure)
%%

diff_with_muscle_wer = (list_muscle_wer - list_wer_thresh(:,4,4)') ./(1-list_muscle_wer);
mean(diff_with_muscle_wer)
std(diff_with_muscle_wer)

diff_with_muscle_bleu = ( list_bleu_thresh(:,4,4)' - list_muscle_bleu) ./(list_muscle_bleu);
mean(diff_with_muscle_bleu)
std(diff_with_muscle_bleu)

diff_with_muscle_fmeasure = ( list_fmeasure_thresh(:,4,4)' - list_muscle_fmeasure) ./(list_muscle_fmeasure);
mean(diff_with_muscle_fmeasure)
std(diff_with_muscle_fmeasure)


%%

%diff_with_graph = (msa_2_15_wer_mean-graph_wer_mean)*100/(1-graph_wer_mean);
%%

%
% graph-based
graph_bleu_mean = mean(list_bleu_graph);
graph_bleu_stdev = std(list_bleu_graph);
% muscle
muscle_bleu_mean = mean(list_muscle_bleu);
muscle_bleu_stdev = std(list_muscle_bleu);

% MSA-A* w = 2 chunk = 5
msa_2_5_bleu_mean = mean(list_bleu_thresh(:,1,4));
msa_2_5_bleu_stdev = std(list_bleu_thresh(:,1,4));
% MSA-A* w = 2 chunk = 10
msa_2_10_bleu_mean = mean(list_bleu_thresh(:,3,4));
msa_2_10_bleu_stdev = std(list_wer_thresh(:,3,4));
% MSA-A* w = 2 chunk = 20
msa_2_15_bleu_mean = mean(list_bleu_thresh(:,4,4));
msa_2_15_bleu_stdev = std(list_bleu_thresh(:,4,4));
% MSA-A* w = 2 chunk = 60
msa_2_60_bleu_mean = mean(list_bleu_thresh(:,end,4));
msa_2_60_bleu_stdev = std(list_bleu_thresh(:,end,4));
% MSA-A* w = 2 chunk = 20, no threshold
msa_2_15_bleu_mean_nothresh = mean(list_bleu_nothresh(:,4,2));
msa_2_15_bleu_stdev_nothresh = std(list_bleu_nothresh(:,4,2));

data = [msa_2_10_bleu_mean; msa_2_15_bleu_mean; msa_2_15_bleu_mean_nothresh; graph_bleu_mean;muscle_bleu_mean];
for i=1:length(data)
    temp = [];
    temp = [num2str(data(i)*100, 4) '%'];
    legendnames2{i} = temp; 
end

figure;h = barweb(data, [ msa_2_10_bleu_stdev ;msa_2_15_bleu_stdev ;msa_2_15_bleu_stdev_nothresh;graph_bleu_stdev;muscle_bleu_stdev],.6, [],'', '' ,'BLEU', 'Jet',[], legendnames,2, 'axis', legendnames2);
set(gca, 'FontSize', 18);

%saveas(gcf, 'bleu_benchmark.epsc');
%saveas(gcf, 'bleu_benchmark.jpg');

%
% graph-based
graph_fmeasure_mean = mean(list_fmeasure_graph);
graph_fmeasure_stdev = std(list_fmeasure_graph);
% muscle
muscle_fmeasure_mean = mean(list_muscle_fmeasure);
muscle_fmeasure_stdev = std(list_muscle_fmeasure);

% MSA-A* w = 2 chunk = 5
msa_2_5_fmeasure_mean = mean(list_fmeasure_thresh(:,1,4));
msa_2_5_fmeasure_stdev = std(list_fmeasure_thresh(:,1,4));
% MSA-A* w = 2 chunk = 10
msa_2_10_fmeasure_mean = mean(list_fmeasure_thresh(:,3,4));
msa_2_10_fmeasure_stdev = std(list_fmeasure_thresh(:,3,4));
% MSA-A* w = 2 chunk = 15
msa_2_15_fmeasure_mean = mean(list_fmeasure_thresh(:,4,4));
msa_2_15_fmeasure_stdev = std(list_fmeasure_thresh(:,4,4));
% MSA-A* w = 2 chunk = 60
msa_2_15_fmeasure_mean_nothresh = mean(list_fmeasure_nothresh(:,end,4));
msa_2_15_fmeasure_stdev_nothresh = std(list_fmeasure_nothresh(:,end,4));
%
% MSA-A* w = 2 chunk = 15
msa_2_15_fmeasure_mean = mean(list_fmeasure_thresh(:,4,4));
msa_2_15_fmeasure_stdev = std(list_fmeasure_thresh(:,4,4));
data = [msa_2_10_fmeasure_mean; msa_2_15_fmeasure_mean; msa_2_15_fmeasure_mean_nothresh;graph_fmeasure_mean;muscle_fmeasure_mean];
for i=1:length(data)
    temp = [];
    temp = [num2str(data(i)*100, 4) '%'];
    legendnames2{i} = temp; 
end
figure;h = barweb(data, [msa_2_10_fmeasure_stdev; msa_2_15_fmeasure_stdev; msa_2_15_fmeasure_stdev_nothresh;graph_fmeasure_stdev;muscle_fmeasure_stdev],.6, [],'', '' ,'F-measure', 'Jet',[], legendnames,2, 'axis',legendnames2);
set(gca, 'FontSize', 16);
saveas(gcf, 'fmeasure_benchmark.epsc');
saveas(gcf, 'fmeasure_benchmark.jpg');
%% DO ALL THE PLOTS FOR WER

%% 
%windex = [1 2 3 4 5 6 7];
windex = [3 4 5 6 7 8 9];
cindex = [1 3 4 5 7 8];

fweight = figure;
fchunk = figure;
clear avg_time;
clear avg_wer;


for c = 1:length(cindex)
    for w=1:length(windex)
        avg_wer(c,w) = mean(list_wer_thresh(:,cindex(c),windex(w)));    
        avg_time(c,w) = mean(list_time(:,cindex(c), windex(w)))/1000;        
    end
end

figure(fweight);
for c = 1:length(cindex)
    hold on; plot(avg_time(c,:), 1-avg_wer(c,:), ['-' marker(c)], 'Color', colors(c,:), 'Linewidth', 2); 
end
set(gca, 'FontSize', 16);
ylabel('1-WER', 'FontSize', 18);
xlabel('Avg Running Time (in Seconds)', 'FontSize',18);
%axis([0 25 0.38 0.58]);

legend('c = 5', 'c = 10', 'c = 15', 'c = 20', 'c = 40', 'c = 60');

saveas(gcf, 'wer_vs_time_for_weight.epsc');
saveas(gcf, 'wer_vs_time_for_weight.jpg');
%%
windex = [3 4 5 6 7 8 9];

figure(fchunk);

clear avg_time;
clear avg_wer;

for c = 1:length(cindex)
    for w=1:length(windex)
        avg_wer(c,w) = mean(list_wer_thresh(:,cindex(c),windex(w)));    
        avg_time(c,w) = mean(list_time(:,cindex(c), windex(w)))/1000;        
    end
end


for w = 1:length(windex)    
    hold on; plot(avg_time(:,w), 1-avg_wer(:,w), ['-' marker(w)], 'Color', colors(w,:), 'Linewidth', 2); 
end
set(gca, 'FontSize', 16);
ylabel('1-WER', 'FontSize', 18);
xlabel('Avg Running Time (in Seconds)', 'FontSize',18);
%axis([0 22 0.4 0.56]);
legend('w = 1.8', 'w = 2', 'w = 2.5', 'w = 3', 'w = 4', 'w = 6', 'w = 8');
saveas(gcf, 'wer_vs_time_for_chunk.epsc');
saveas(gcf, 'wer_vs_time_for_chunk.jpg');
%%

%%%%%%%%%%%%%%%%%% Avg timing %%%%%%%%%%%%%%%%%%%%
 avg_time_to_process_chunk = mean(list_time(:,4,4))*chunk_sizes(4)/320;
