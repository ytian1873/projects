import AWS from 'aws-sdk';
import { Context, Callback, APIGatewayEvent, APIGatewayProxyResult, Handler } from 'aws-lambda';
import { resolve } from 'dns';


export const handler: Handler<APIGatewayEvent> =
    async (event: APIGatewayEvent, context: Context, callback: Callback): Promise<APIGatewayProxyResult> => {
        var paths = event.path.split("/");
        console.log(paths)

        if (paths.length != 9) {
            return {
                statusCode: 404,
                body: JSON.stringify({ "message": "Invalid URL" })
            };
        } 

        var liftId = '-1';
        var liftTime = '-1';

        if (event.body) {
            let body = JSON.parse(event.body)
            if (body.time) 
                liftTime = body.time.toString();
            if (body.liftID) 
                liftId = body.liftID.toString();
        }

        // if (event.queryStringParameters != null && event.queryStringParameters.LiftId && event.queryStringParameters.LiftTime) {
        //     liftId = event.queryStringParameters.LiftId;
        //     liftTime = event.queryStringParameters.LiftTime;
        // }

        var verticalRise = (parseInt(liftId) * 10).toString(10);
        var dayId = paths[6] + '.' + Math.floor((Math.random()*1000000)+1)

        var postQuery = {
            TableName: 'skiers',
            Item: {
                'SkierId': { N: paths[8] },
                'DayId': { N: dayId },
                'LiftTime': { N: liftTime },
                'LiftId': { N: liftId },
                'ResortId': { N: paths[2] },
                'SeasonId': { N: paths[4] },
                'VerticalRise': { N: verticalRise }
            }
        };

        var query = {
            TableName: 'skiers',
            KeyConditionExpression: 'SkierId = :s and DayId between :d1 and :d2',
            ExpressionAttributeValues: {
                ':s': { N: paths[8] },
                ':d1': { N: paths[6] }, 
                ':d2': { N: (parseFloat(paths[6]) + 1).toString(10) }, 
            },
            ProjectionExpression: "VerticalRise",
        };

        const dynamo = new AWS.DynamoDB();

        if (event.httpMethod === 'POST') {
            await dynamo.putItem(postQuery).promise();
            return {
                statusCode: 200,
                body: JSON.stringify({ "message": "success" })
            };
        } else if (event.httpMethod === 'GET') {
            const data = await dynamo.query(query).promise();
            return {
                statusCode: 200,
                body: JSON.stringify(data)
            };
        }
        return {
            statusCode: 400,
            body: JSON.stringify({ "message": "Cannot access database." })
        }
    }
