"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
var aws_sdk_1 = __importDefault(require("aws-sdk"));
exports.handler = function (event, context, callback) { return __awaiter(void 0, void 0, void 0, function () {
    var paths, liftId, liftTime, body, verticalRise, dayId, postQuery, query, dynamo, data;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                paths = event.path.split("/");
                console.log(paths);
                if (paths.length != 9) {
                    return [2 /*return*/, {
                            statusCode: 404,
                            body: JSON.stringify({ "message": "Invalid URL" })
                        }];
                }
                liftId = '-1';
                liftTime = '-1';
                if (event.body) {
                    body = JSON.parse(event.body);
                    if (body.time)
                        liftTime = body.time.toString();
                    if (body.liftID)
                        liftId = body.liftID.toString();
                }
                verticalRise = (parseInt(liftId) * 10).toString(10);
                dayId = paths[6] + '.' + Math.floor((Math.random() * 1000000) + 1);
                postQuery = {
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
                query = {
                    TableName: 'skiers',
                    KeyConditionExpression: 'SkierId = :s and DayId between :d1 and :d2',
                    ExpressionAttributeValues: {
                        ':s': { N: paths[8] },
                        ':d1': { N: paths[6] },
                        ':d2': { N: (parseFloat(paths[6]) + 1).toString(10) },
                    },
                    ProjectionExpression: "VerticalRise",
                };
                dynamo = new aws_sdk_1.default.DynamoDB();
                if (!(event.httpMethod === 'POST')) return [3 /*break*/, 2];
                return [4 /*yield*/, dynamo.putItem(postQuery).promise()];
            case 1:
                _a.sent();
                return [2 /*return*/, {
                        statusCode: 200,
                        body: JSON.stringify({ "message": "success" })
                    }];
            case 2:
                if (!(event.httpMethod === 'GET')) return [3 /*break*/, 4];
                return [4 /*yield*/, dynamo.query(query).promise()];
            case 3:
                data = _a.sent();
                return [2 /*return*/, {
                        statusCode: 200,
                        body: JSON.stringify(data)
                    }];
            case 4: return [2 /*return*/, {
                    statusCode: 400,
                    body: JSON.stringify({ "message": "Cannot access database." })
                }];
        }
    });
}); };
