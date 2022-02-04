# SQLCipher Performance with Room

The purpose of the app is to benchmark the performance of SQLite and Room when SQLCipher plugin is enabled.

## What can be benchmarked
|        | No Encryption | Encryption | Encryption with Memory Security |
|--------|---------------|------------|---------------------------------|
| Insert | ✅             | ✅          | ✅                           |
| Select indexed | ✅             | ✅          | ✅           | 
| Select NOT indexed | ✅             | ✅          | ✅           | 

## Download the App on PlayStore
You can download SQLCipher Performance on the PlayStore here

## Some results

|  10,000 transactions | No Encryption | Encryption | Encryption with Memory Security |
|--------|---------------|------------|---------------------------------|
| Insert | 443ms            | 453ms (2.3% slower)          | 563ms (27% slower)                           |
| Select indexed | 3711ms            | 3836ms (3.4 % slower)         | 3882ms (4.6% slower)          | 
| Select NOT indexed | 75,462ms             | 450,329ms (496% slower)        | 1,027,048ms (1261% slower)         | 


Performed on Nokia 6.1 (Qualcomm Snapdragon 636 1.80GHz, 8-core, Kryo 260, on Battery)

## Data
### Object
```kotlin
@Entity(tableName = "person", indices = [Index(value = ["id"], unique = true)])
data class Person (
    @PrimaryKey val id: Long? = null,
    @ColumnInfo(name = "first_name") val firstName: String? = null,
    @ColumnInfo(name = "last_name") val lastName: String? = null,
    @ColumnInfo(name = "height") val height: Double? = null,
    @ColumnInfo(name = "weight") val weight: Double? = null,
    @ColumnInfo(name = "cv_info") val cvInfo: String? = null
)
```

### Queries

####Insert
Uses Room insert, which is transactional (all data at once) similar to

```sql
INSERT INTO person (id, first_name, last_name, height, weight, cv_info) 
VALUES (1, "a", "b", 1.0, 1.0, "c"),
(2, "a", "b", 1.0, 1.0, "c")
...
(n, "a", "b", 1.0, 1.0, "c")

```


####Select indexed
Index will help find the data very quickly. 

```sql
SELECT * FROM person WHERE id = :id
```

#### Select NOT indexed
without index, SQLite is going to go through each record to find matches. this help to understand how full text search will work.

```sql
SELECT * FROM person WHERE first_name LIKE :find
```


## Screenshots


![](https://github.com/sonique6784/SQLCipherPerformance/raw/master/screenshots/SQLCipherPerformance-UI.png)

![](https://github.com/sonique6784/SQLCipherPerformance/raw/master/screenshots/SQLCipherPerformance-UI.png)


## Google Assistant
You can use Google Assistant to start some of the commands:
you can say:
 - **Insert** with SQLCipherPerformance
 - **Select** with SQLCipherPerformance

or launch the deeplink manually:

```
adb shell am start -a android.intent.action.VIEW -d "app://sqlcipherperformance.sonique.fr/open?feature=INSERT"
```

### Slices
Give a try to Google Assistant Slices
```
adb shell am start -a android.intent.action.VIEW -d
    slice-content://fr.sonique.sqlcipherperformance/slice
```


## Supported by ADN
[Android Developer News](https://play.google.com/store/apps/details?id=sonique.fr.adn) is sponsoring this app.

Stay on top of Android Development, [download the app](https://play.google.com/store/apps/details?id=sonique.fr.adn)

