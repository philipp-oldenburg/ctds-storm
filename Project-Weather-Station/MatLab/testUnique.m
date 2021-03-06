%% Train
readData = csvread('cleandataTranslatedSimplifiedUnique.csv')';
difference = [[0;0;0;0] , diff(readData(1:4,:)')'];
mainData = [ readData(1:4,:) ; difference ; readData(5,:) ];

ind = find(mainData(9,:) == 0);
dataClear = mainData(1:8,ind);
ind = find(mainData(9,:) == 1);
dataRain = mainData(1:8,ind);
ind = find(mainData(9,:) == 2);
dataSnow = mainData(1:8,ind);
ind = find(mainData(9,:) == 3);
dataClouds = mainData(1:8,ind);
ind = find(mainData(9,:) == 4);
dataMist = mainData(1:8,ind);
ind = find(mainData(9,:) == 5);
dataFog = mainData(1:8,ind);
ind = find(mainData(9,:) == 6);
dataDrizzle = mainData(1:8,ind);
ind = find(mainData(9,:) == 7);
dataThunderstorm = mainData(1:8,ind);

meanClear = sum(dataClear, 2) ./ size(dataClear,2);
meanRain = sum(dataRain, 2) ./ size(dataRain,2);
meanSnow = sum(dataSnow, 2) ./ size(dataSnow,2);
meanClouds = sum(dataClouds, 2) ./ size(dataClouds,2);
meanMist = sum(dataMist, 2) ./ size(dataMist,2);
meanFog = sum(dataFog, 2) ./ size(dataFog,2);
meanDrizzle = sum(dataDrizzle, 2) ./ size(dataDrizzle,2);
meanThunderstorm = sum(dataThunderstorm, 2) ./ size(dataThunderstorm,2);

%calculate covariance matrix for Clear
covClear = zeros(size(dataClear, 1));
for i = 1:size(dataClear,2)
    x = (dataClear(:,i) - meanClear);
    covClear = covClear + (x * x');
end
covClear = covClear / size(dataClear,2);

%calculate covariance matrix for Rain
covRain = zeros(size(dataRain, 1));
for i = 1:size(dataRain,2)
    x = (dataRain(:,i) - meanRain);
    covRain = covRain + (x * x');
end
covRain = covRain / size(dataRain,2);

%calculate covariance matrix for Snow
covSnow = zeros(size(dataSnow, 1));
for i = 1:size(dataSnow,2)
    x = (dataSnow(:,i) - meanSnow);
    covSnow = covSnow + (x * x');
end
covSnow = covSnow / size(dataSnow,2);

%calculate covariance matrix for Clouds
covClouds = zeros(size(dataClouds, 1));
for i = 1:size(dataClouds,2)
    x = (dataClouds(:,i) - meanClouds);
    covClouds = covClouds + (x * x');
end
covClouds = covClouds / size(dataClouds,2);

%calculate covariance matrix for Mist
covMist = zeros(size(dataMist, 1));
for i = 1:size(dataMist,2)
    x = (dataMist(:,i) - meanMist);
    covMist = covMist + (x * x');
end
covMist = covMist / size(dataMist,2);

%calculate covariance matrix for Fog
covFog = zeros(size(dataFog, 1));
for i = 1:size(dataFog,2)
    x = (dataFog(:,i) - meanFog);
    covFog = covFog + (x * x');
end
covFog = covFog / size(dataFog,2);

%calculate covariance matrix for Drizzle
covDrizzle = zeros(size(dataDrizzle, 1));
for i = 1:size(dataDrizzle,2)
    x = (dataDrizzle(:,i) - meanDrizzle);
    covDrizzle = covDrizzle + (x * x');
end
covDrizzle = covDrizzle / size(dataDrizzle,2);

%calculate covariance matrix for Thunderstorm
covThunderstorm = zeros(size(dataThunderstorm, 1));
for i = 1:size(dataThunderstorm,2)
    x = (dataThunderstorm(:,i) - meanThunderstorm);
    covThunderstorm = covThunderstorm + (x * x');
end
covThunderstorm = covThunderstorm / size(dataThunderstorm,2);

%% Extract Values
format long;

disp('Clear:');
normClear = 1./(sqrt((2*pi)^(length(meanClear)) * det(covClear)));
invClear = (covClear^-1);
disp(normClear);
disp(meanClear);
disp(invClear);
norm = normClear;
mean = meanClear';
inv = invClear;
prior = size(dataClear,2) / size(mainData,2);
save('Clear.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Rain:');
normRain = 1./(sqrt((2*pi)^(length(meanRain)) * det(covRain)));
invRain = (covRain^-1);
disp(normRain);
disp(meanRain);
disp(invRain);
norm = normRain;
mean = meanRain';
inv = invRain;
prior = size(dataRain,2) / size(mainData,2);
save('Rain.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Snow:');
normSnow = 1./(sqrt((2*pi)^(length(meanSnow)) * det(covSnow)));
invSnow = (covSnow^-1);
disp(normSnow);
disp(meanSnow);
disp(invSnow);
norm = normSnow;
mean = meanSnow';
inv = invSnow;
prior = size(dataSnow,2) / size(mainData,2);
save('Snow.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Clouds:');
normClouds = 1./(sqrt((2*pi)^(length(meanClouds)) * det(covClouds)));
invClouds = (covClouds^-1);
disp(normClouds);
disp(meanClouds);
disp(invClouds);
norm = normClouds;
mean = meanClouds';
inv = invClouds;
prior = size(dataClouds,2) / size(mainData,2);
save('Clouds.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Mist:');
normMist = 1./(sqrt((2*pi)^(length(meanMist)) * det(covMist)));
invMist = (covMist^-1);
disp(normMist);
disp(meanMist);
disp(invMist);
norm = normMist;
mean = meanMist';
inv = invMist;
prior = size(dataMist,2) / size(mainData,2);
save('Mist.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Fog:');
normFog = 1./(sqrt((2*pi)^(length(meanFog)) * det(covFog)));
invFog = (covFog^-1);
disp(normFog);
disp(meanFog);
disp(invFog);
norm = normFog;
mean = meanFog';
inv = invFog;
prior = size(dataFog,2) / size(mainData,2);
save('Fog.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Drizzle:');
normDrizzle = 1./(sqrt((2*pi)^(length(meanDrizzle)) * det(covDrizzle)));
invDrizzle = (covDrizzle^-1);
disp(normDrizzle);
disp(meanDrizzle);
disp(invDrizzle);
norm = normDrizzle;
mean = meanDrizzle';
inv = invDrizzle;
prior = size(dataDrizzle,2) / size(mainData,2);
save('Drizzle.cluster.mat', 'norm', 'mean', 'inv', 'prior');

disp('Thunderstorm:');
normThunderstorm = 1./(sqrt((2*pi)^(length(meanThunderstorm)) * det(covThunderstorm)));
invThunderstorm = (covThunderstorm^-1);
disp(normThunderstorm);
disp(meanThunderstorm);
disp(invThunderstorm);
norm = normThunderstorm;
mean = meanThunderstorm';
inv = invThunderstorm;
prior = size(dataThunderstorm,2) / size(mainData,2);
save('Thunderstorm.cluster.mat', 'norm', 'mean', 'inv', 'prior');

%% Try

p0_norm = 1./(sqrt((2*pi)^(length(meanClear)) * det(covClear)));
p0_inv = (covClear^-1);
p0 = @(x) p0_norm * exp(-(1/2)*(x-meanClear)'* p0_inv *(x-meanClear));

p0([4.92222256130642;998;93;3.24000000953674;0;0;0;0])
