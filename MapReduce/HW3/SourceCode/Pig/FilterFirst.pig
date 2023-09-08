register file:/usr/lib/pig/lib/piggybank.jar 
define CSVLoader org.apache.pig.piggybank.storage.CSVLoader;

-- Load file to Flights1 andd Flights2
Flights1 = load '$INPUT' using CSVLoader as (Year:int,Quarter,Month:int,DayofMonth,DayOfWeek,FlightDate,UniqueCarrier,AirlineID,Carrier,TailNum,FlightNum,Origin,OriginCityName,OriginState,OriginStateFips,
                                             OriginStateNam,OriginWac,Dest,DestCityName,DestState,DestStateFips,DestStateName,DestWac,CRSDepTime,DepTime,DepDelay,DepDelayMinutes,DepDel15,
                                             DepartureDelayGroups,DepTimeBlk,TaxiOut,WheelsOff,WheelsOn,TaxiIn,CRSArrTime,ArrTime:int,ArrDelay,ArrDelayMinutes:int,ArrDel15,ArrivalDelayGroups,ArrTimeBlk,Cancelled,
                                             CancellationCode,Diverted,CRSElapsedTime,ActualElapsedTime,AirTime,Flights,Distance,DistanceGroup,CarrierDelay,WeatherDelay,NASDelay,SecurityDelay,LateAircraftDelay);

Flights2 = load '$INPUT' using CSVLoader as (Year:int,Quarter,Month:int,DayofMonth,DayOfWeek,FlightDate,UniqueCarrier,AirlineID,Carrier,TailNum,FlightNum,Origin,OriginCityName,OriginState,OriginStateFips,
                                             OriginStateNam,OriginWac,Dest,DestCityName,DestState,DestStateFips,DestStateName,DestWac,CRSDepTime,DepTime:int,DepDelay,DepDelayMinutes,DepDel15,
                                             DepartureDelayGroups,DepTimeBlk,TaxiOut,WheelsOff,WheelsOn,TaxiIn,CRSArrTime,ArrTime,ArrDelay,ArrDelayMinutes:int,ArrDel15,ArrivalDelayGroups,ArrTimeBlk,Cancelled,
                                             CancellationCode,Diverted,CRSElapsedTime,ActualElapsedTime,AirTime,Flights,Distance,DistanceGroup,CarrierDelay,WeatherDelay,NASDelay,SecurityDelay,LateAircraftDelay);

-- Remove columns that are not used
First =  foreach Flights1 generate Year, Month, FlightDate, Origin, Dest, ArrTime, DepTime, ArrDelayMinutes, Cancelled, Diverted;
Second = foreach Flights2 generate Year, Month, FlightDate, Origin, Dest, ArrTime, DepTime, ArrDelayMinutes, Cancelled, Diverted;

-- Filter out flight with conditions
FirstFlight = filter First by (((Year == 2007 and Month >= 6) or (Year == 2008 and Month <= 5)) and Origin == 'ORD' and Dest != 'JFK' and Cancelled == '0.00' and Diverted == '0.00');
SecondFlight = filter Second by ((Year == 2007 and Month >= 6) or (Year == 2008 and Month <= 5) and Origin != 'ORD' and Dest == 'JFK' and Cancelled == '0.00' and Diverted == '0.00');

-- Join data, and remove those flights which time doesn't match
JoinFlight = join FirstFlight by (FlightDate, Dest), SecondFlight by (FlightDate, Origin);
Filtered = filter JoinFlight by (FirstFlight::ArrTime < SecondFlight::DepTime);

-- Calculate average delay
Delay = foreach Filtered generate (FirstFlight::ArrDelayMinutes + SecondFlight::ArrDelayMinutes) as delay;
Grouped = group Delay all;
AvgDelay = foreach Grouped generate AVG(Delay);

store AvgDelay into '$OUTPUT';
