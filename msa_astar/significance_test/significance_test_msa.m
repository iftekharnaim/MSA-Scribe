A = dlmread('significance_test.data');
weighted_error_msa = A(:,1)' * A(:,3)/ sum(A(:,3));
weighted_error_graph = A(:,2)' * A(:,3)/ sum(A(:,3));


error_diff = abs(A(:,1)-A(:,2));
n = 4;

t = mean(error_diff)/sqrt(var(error_diff)/n);

%  ./signif -a .4696 -b .5325 -n 2862
