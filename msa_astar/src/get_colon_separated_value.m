function val = get_colon_separated_value(str)


[token,rem] = strtok(str, ':');
val = str2double(rem(2:end));
