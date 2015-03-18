data = [2,0.8491260923845191,53.8;3,0.734207240948814,68.5;4,0.6275280898876405,122.4;5,0.5759051186017479,197.85;6,0.514856429463171,362.5;7,0.4705992509363296,711.55;8,0.45349563046192254,1479.75;9,0.44431960049937597,3539.9;10,0.3982521847690386,9618.65];

data(:,3) = data(:,3)/1000;

figure;plot((data(:,3)), 1-data(:,2), 'bd-', 'Linewidth',2);
set(gca, 'FontSize', 16);
ylabel('Average (1-WER)', 'FontSize', 18);
xlabel('Average Running Time (in sec)', 'FontSize',18);
axis([-0.5 11 0 0.65]);
%%

figure;plot(log(data(:,3)), 1-data(:,2), 'bd-', 'Linewidth',3);

set(gca, 'FontSize', 16);
ylabel('Avg (1-WER)', 'FontSize', 18);
xlabel('Avg Log Running Time (in Log-msec)', 'FontSize',18);
