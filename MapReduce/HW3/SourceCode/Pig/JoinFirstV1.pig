register file:/usr/lib/pig/lib/piggybank.jar 
define CSVLoader org.apache.pig.piggybank.storage.CSVLoader;

Flights1 = load '$INPUT' using CSVLoader as (Year:int,Quarter,Month:int,DayofMonth,DayOfWeek,FlightDate,UniqueCarrier,AirlineID,Carrier,TailNum,FlightNum,Origin,OriginCityName,OriginState,OriginStateFips,
                                             OriginStateNam,OriginWac,Dest,DestCityName,DestState,DestStateFips,DestStateName,DestWac,CRSDepTime,DepTime,DepDelay,DepDelayMinutes,DepDel15,
                                             DepartureDelayGroups,DepTimeBlk,TaxiOut,WheelsOff,WheelsOn,TaxiIn,CRSArrTime,ArrTime:int,ArrDelay,ArrDelayMinutes:int,ArrDel15,ArrivalDelayGroups,ArrTimeBlk,Cancelled,
                                             CancellationCode,Diverted,CRSElapsedTime,ActualElapsedTime,AirTime,Flights,Distance,DistanceGroup,CarrierDelay,WeatherDelay,NASDelay,SecurityDelay,LateAircraftDelay);

Flights2 = load '$INPUT' using CSVLoader as (Year:int,Quarter,Month:int,DayofMonth,DayOfWeek,FlightDate,UniqueCarrier,AirlineID,Carrier,TailNum,FlightNum,Origin,OriginCityName,OriginState,OriginStateFips,
                                             OriginStateNam,OriginWac,Dest,DestCityName,DestState,DestStateFips,DestStateName,DestWac,CRSDepTime,DepTime:int,DepDelay,DepDelayMinutes,DepDel15,
                                             DepartureDelayGroups,DepTimeBlk,TaxiOut,WheelsOff,WheelsOn,TaxiIn,CRSArrTime,ArrTime,ArrDelay,ArrDelayMinutes:int,ArrDel15,ArrivalDelayGroups,ArrTimeBlk,Cancelled,
                                             CancellationCode,Diverted,CRSElapsedTime,ActualElapsedTime,AirTime,Flights,Distance,DistanceGroup,CarrierDelay,WeatherDelay,NASDelay,SecurityDelay,LateAircraftDelay);
                                        
FirstCleaned1 = foreach Flights1 generate Year, Month, FlightDate, Origin, Dest, ArrDelayMinutes, ArrTime, DepTime, Cancelled, Diverted;
FirstCleaned2 = foreach Flights2 generate Year, Month, FlightDate, Origin, Dest, ArrDelayMinutes, ArrTime, DepTime, Cancelled, Diverted;

FirstFiltered1 = filter FirstCleaned1 by (Origin == 'ORD' and Dest != 'JFK' and Cancelled == '0.00' and Diverted == '0.00');
FirstFiltered2 = filter FirstCleaned2 by (Origin != 'ORD' and Dest == 'JFK' and Cancelled == '0.00' and Diverted == '0.00');

JointData = join FirstFiltered1 by (Dest, FlightDate), FirstFiltered2 by (Origin, FlightDate);

FilteredJoint = filter JointData by FirstFiltered1::ArrTime <= FirstFiltered2::DepTime;

FinalFiltered = filter FilteredJoint by ((FirstFiltered1::Year == 2007 and FirstFiltered1::Month >= 6) or (FirstFiltered1::Year == 2008 and FirstFiltered1::Month <= 5) 
                                         and (FirstFiltered2::Year == 2007 and FirstFiltered2::Month >= 6) or (FirstFiltered2::Year == 2008 and FirstFiltered2::Month <= 5));
TotalDelay = foreach FinalFiltered generate (FirstFiltered1::ArrDelayMinutes + FirstFiltered2::ArrDelayMinutes) as total;
Grouped = group TotalDelay all;
AvgDelay = foreach Grouped generate AVG(TotalDelay);

store AvgDelay into '$OUTPUT';
