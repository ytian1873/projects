CREATE SCHEMA IF NOT EXISTS BonAppitit;
USE BonAppitit;

DROP TABLE IF EXISTS SearchHistories;
DROP TABLE IF EXISTS UserTagRecipe;
DROP TABLE IF EXISTS UserFavoriesTag;
DROP TABLE IF EXISTS RecipesRates;
DROP TABLE IF EXISTS MeatRecipes;
DROP TABLE IF EXISTS VegetableRecipes;
DROP TABLE IF EXISTS SeafoodRecipes;
DROP TABLE IF EXISTS DairyRecipes;
DROP TABLE IF EXISTS DessertRecipes;
DROP TABLE IF EXISTS Recommendation;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Recipes;

  
CREATE TABLE Users(
 UserId INT NOT NULL,
 CONSTRAINT pk_Users_UserId PRIMARY KEY (UserId)
);

CREATE TABLE Recipes(
 RecipeId INT NOT NULL,
 RecipeName TINYTEXT,
 ImageUrl TINYTEXT,
 RecipeIngredients TEXT,
 CookingDirections TEXT,
 Nutritions TEXT,
 CONSTRAINT pk_Recipe_RecipeId PRIMARY KEY (RecipeId)
);

CREATE TABLE SearchHistories (
 UserId INT NOT NULL,
 RecipeId INT NOT NULL,
 RecipeName TINYTEXT,
 CONSTRAINT pk_SearchHistories_UserId_RecipeId PRIMARY KEY (UserId, RecipeId),
 CONSTRAINT fk_SearchHistories_UserId FOREIGN KEY (UserId)
 REFERENCES Users(UserId)
 ON UPDATE CASCADE ON DELETE CASCADE,
CONSTRAINT fk_SearchHistories_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE UserFavoriesTag (
 FavoriesId INT NOT NULL,
 UserId INT NOT NULL,
 CONSTRAINT pk_UserFavories_FavoriesId PRIMARY KEY (FavoriesId),
 CONSTRAINT fk_UserFavories_UserId FOREIGN KEY (UserId)
 REFERENCES Users(UserId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE UserTagRecipe (
 TagId INT NOT NULL,
 FavoriesId INT NOT NULL,
 UserId INT NOT NULL,
 RecipeId INT,
 CONSTRAINT pk_UserTag_TagId PRIMARY KEY (TagId),
 CONSTRAINT fk_UserTag_FavoriesId FOREIGN KEY (FavoriesId)
 REFERENCES UserFavoriesTag(FavoriesId)
 ON UPDATE CASCADE ON DELETE CASCADE,
 CONSTRAINT fk_UserTag_UserId FOREIGN KEY (UserId)
 REFERENCES Users(UserId)
 ON UPDATE CASCADE ON DELETE CASCADE,
 CONSTRAINT fk_UserTag_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE RecipesRates(
 UserId INT,
 RecipeId INT,
 Rating INT NOT NULL,
 LastModified TEXT,
 CONSTRAINT pk_RecipesRates_UserId PRIMARY KEY (UserId, RecipeId),
 CONSTRAINT fk_RecipesRates_UserId_RecipeId FOREIGN KEY (UserId)
 REFERENCES Users(UserId)
 ON UPDATE CASCADE ON DELETE CASCADE,
 CONSTRAINT fk_RecipesRates_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE MeatRecipes(
 RecipeId INT,
 CONSTRAINT pk_MeatRecipes_RecipeId PRIMARY KEY (RecipeId),
 CONSTRAINT fk_MeatRecipes_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE VegetableRecipes(
 RecipeId INT,
 CONSTRAINT pk_VegetableRecipes_RecipeId PRIMARY KEY (RecipeId),
 CONSTRAINT fk_VegetableRecipes_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE SeafoodRecipes(
 RecipeId INT,
 CONSTRAINT pk_SeafoodRecipes_RecipeId PRIMARY KEY (RecipeId),
 CONSTRAINT fk_SeafoodRecipes_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE DairyRecipes(
 RecipeId INT,
 CONSTRAINT pk_DairyRecipes_RecipeId PRIMARY KEY (RecipeId),
 CONSTRAINT fk_DairyRecipes_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE DessertRecipes(
 RecipeId INT,
 CONSTRAINT pk_DessertRecipes_RecipeId PRIMARY KEY (RecipeId),
 CONSTRAINT fk_DessertRecipes_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Recommendation(
 RecommendationId INT NOT NULL,
 RecipeId INT,
 RecipeName TINYTEXT,
 Rating INT NOT NULL,
 CONSTRAINT pk_Recommendation_RecommendationId PRIMARY KEY (RecommendationId),
 CONSTRAINT fk_Recommendation_RecipeId FOREIGN KEY (RecipeId)
 REFERENCES Recipes(RecipeId)
 ON UPDATE CASCADE ON DELETE SET NULL
);

SET GLOBAL local_infile = 1;

LOAD DATA LOCAL INFILE '/Users/y.tian/Desktop/NEU/fall19/CS5200/BonAppitit_Data/user-name.csv' INTO TABLE Users
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\r\n'
  IGNORE 1 LINES;
  
LOAD DATA LOCAL INFILE '/Users/y.tian/Desktop/NEU/fall19/CS5200/BonAppitit_Data/core-data_recipe.csv' INTO TABLE Recipes
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\r\n'
  IGNORE 1 LINES;

LOAD DATA LOCAL INFILE '/Users/y.tian/Desktop/NEU/fall19/CS5200/BonAppitit_Data/core-data-train_rating.csv' INTO TABLE RecipesRates
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\r\n'
  IGNORE 1 LINES;