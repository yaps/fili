Fili API指南
==================

Fili API提供数据访问，亦是Fili的核心功能。

API使用HTTPS `GET`访问模式, 你的访问查询语句就是一个简单的URL，便于记录和共享。

目录
-----------------

- [核心概念](#core-concepts)
    - [数据维度](#dimensions)
    - [数据度量](#metrics)
    - [列表](#tables)
    - [数据筛选](#filters)
    - [时间段](#interval)
    - [时间单位](#time-grain)
- [数据访问](#data-queries)
    - [基本介绍](#basics)
    - [Dimension Breakout](#dimension-breakout)
    - [Filtering Example](#filtering-example)
    - [Response Format Example](#response-format-example)
- [Query Options](#query-options)
    - [Pagination / Limit](#pagination--limit)
    - [Response Format](#response-format)
    - [Filtering](#filtering)
    - [Having](#having)
    - [Sorting](#sorting)
    - [TopN](#topn)
- [Asynchronous Queries](#asynchronous-queries)
    - [Jobs endpoint](#jobs-endpoint)
- [Misc](#misc)
    - [Dates and Times](#dates-and-times)
      - [Date Periods](#date-periods)
      - [Date Macros](#date-macros)
      - [Time Zone](#time-zone)
    - [Case Sensitivity](#case-sensitivity)
    - [Rate Limiting](#rate-limiting)
    - [Errors](#errors)

核心概念
-------------

Fili包含五个主要概念：

- [数据维度](#dimensions)
- [数据度量](#metrics)
- [列表](#tables)
- [数据筛选](#filters)
- [时间单位](#time-grain)
- [时间段](#interval)

### 数据维度 ###

数据维度代表所有的数据切分，一个维度代表数据在某一项内容的不同的值。维度用于数据分类，归纳，筛选，作用十分关键。每个维度
有包含一些变量，和所有可能的值。这些变量和值主要用于数据筛选和标注访问结果。

所有维度包含一个ID变量（用作数据库的key）和关于这个维度的描述，这两项可以用来过滤返回结果（rows），API的返回结果亦会包含
这两项。

访问[所有维度](https://sampleapp.fili.org/v1/dimensions):

    GET https://sampleapp.fili.org/v1/dimensions

访问[某一维度](https://sampleapp.fili.org/v1/dimensions/productRegion):

    GET https://sampleapp.fili.org/v1/dimensions/productRegion

访问[某一维度的所有可能值](https://sampleapp.fili.org/v1/dimensions/productRegion/values):

    GET https://sampleapp.fili.org/v1/dimensions/productRegion/values

除此之外，访问维度的值还包含一些访问可选项：

- [分页](#pagination--limit)
- [格式](#response-format)
- [筛选](#filtering) (各种筛选方式)

例如，访问[维度描述包含"U"的用户国家，第二页，每页五项结果，用JSON表示](https://sampleapp.fili.org/v1/dimensions/userCountry/values?filters=userCountry|desc-contains[U]&page=2&perPage=5&format=json):

    GET https://sampleapp.fili.org/v1/dimensions/userCountry/values?filters=userCountry|desc-contains[U]&page=2&perPage=5&format=json

### 数据度量 ###

度量实际上就是实际数据，例如网页访问量，日均用时等等。度量取决于某个数据列表和时间单位，和所有支持的度量内容。

访问[所有数据](https://sampleapp.fili.org/v1/metrics):

    GET https://sampleapp.fili.org/v1/metrics
    
访问[某一数据](https://sampleapp.fili.org/v1/metrics/timeSpent):

    GET https://sampleapp.fili.org/v1/metrics/timeSpent

### 列表 ###

知道了[数据](#metrics)，[维度](#dimensions)，[时间单位](#time-grain)，将两者合并到一起的就是列表了，给定一个列表和时间单
位，就会给出一组可访问的数据和维度。

访问[所有列表](https://sampleapp.fili.org/v1/tables):

    GET https://sampleapp.fili.org/v1/tables

访问[某个列表](https://sampleapp.fili.org/v1/tables/network/week):

    GET https://sampleapp.fili.org/v1/tables/network/week

### 数据筛选 ###

筛选功能可以给返回的数据添加限定条件。筛选功能可以过滤某一行或多行结果（row），也可以整体筛选数据。

有些访问不是针对数据值，对于这些访问，由于没有数据合并，筛选功能在这里主要是增删数据行（row）。

而对于[那些针对数据值的访问/数据访问](#data-queries)，增删数据就会出现在数据合并过的数据行里。如果需要筛选数据访问结果，
访问请求必须在维度筛选中附上一个[维度路径](#dimension-breakout)。

### 时间段 ###

访问时间段（`dateTime`）指的是数据所在的时间范围。时间段用一个起始时间和终止时间表示，这里的时间采用ISO 8601格式，即包含
其实时间，不包含终止时间。用户可以很快适应这种时间段的表述方式。

特别注意，访问的时间段必须和[时间单位](#time-grain)相吻合。例如，如果时间单位是以月为单位，时间段必须以月份起止，如果是
以星期为单位，则必须以星期一作为起止。

### 时间单位 ###

时间单位是每个返回数据行的数据时间跨度，换言之，时间单位是数据合并的时间单元。In particular, this matters a lot for metrics that are counting "unique"
things, like Unique Identifier.

目前支持的时间单位有秒，分钟，小时，天，星期，月，季度，年。还有一个"全部（all）"时间单位，把所有数据合并到一个单元里。

数据访问
------------

数据访问是Fili API的关键功能，提供数据的

- [根据维度]分组(#dimensions)
- [根据时间单位]分组(#time-grain)
- 根据维度值[筛选数据](#filtering)
- 实现[Having](#having)筛选
- Selecting [metrics](#metrics)
- 返回某个[时间段](#interval)的数据
- 某个时间单位内的[数据排序](#sorting)
- 选择[返回数据格式](#response-format)

### 基本介绍 ###

我们先举例讲解一下访问URL的格式：

    GET https://sampleapp.fili.io/v1/data/network/week?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08

这是一个最[基本的查询语句](https://sampleapp.fili.io/v1/data/network/week?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08)，
返回的是一个星期以内，网页访问数和日均在线时间的数据。我们来具体讲解一下其组成。

- **https\://** - Fili API用安全连接传输数据, _必须使用HTTPS_。_HTTP访问无效_。
- **sampleapp.fili.org** - Fili API的服务器地址。
- **v1** - API版本。
- **data** - 查询访问的资源类别，亦是所有数据查询必须的指示内容。
- **network** - Network是提供数据的[列表](#tables)。
- **week** - 返回数据的[时间单位](#time-grain)。每一行数据以星期为单位合并。
- **metrics** - 所有返回数据的[数据度量](#metrics)，多个度量用逗号隔开（注意：拼写是区分大小写的）。
- **dateTime** - 所有数据所处的[时间段](#interval)，用[ISO 8601格式](#dates-and-times)表示。

### 维度叠加 ###

了解了基本的查询语句后，有人也许想查询维度上的数据，例如[Product Region维度](https://sampleapp.fili.io/v1/data/network/week/productRegion?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08)?

    GET https://sampleapp.fili.io/v1/data/network/week/productRegion?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08

我们在原有的URL上添加了一段路径(`productRegion`)。任意数量的维度都可以在[时间单位](#time-grain)之后添加。

照此原理，如果我们还想[添加`gender`](https://sampleapp.fili.io/v1/data/network/week/productRegion/gender?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08)
（一共两个维度）：

    GET https://sampleapp.fili.io/v1/data/network/week/productRegion/gender?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08

### 数据筛选 ###

叠加了维度之后，我们还可以在此基础上筛选数据。比如说我们想得到[美国以外的全球数据](https://sampleapp.fili.io/v1/data/network/week?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08&filters=productRegion|id-notin[Americas+Region])?

    GET https://sampleapp.fili.io/v1/data/network/week?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08&filters=productRegion|id-notin[Americas Region]

我们在查询语句里添加了一个[筛选项](#filtering)，过滤掉(`notin`)`productRegion`维度里ID是`Americas Region`的数据行。我们
去掉了`productRegion`这个分组维度，因为我们需要全球的整体数据，加了分组维度只会按地区显示某个地区的数据。

这个例子向我们展示了如何在某一维度没有分组的时候，筛选这个维度里的数据。筛选功能十分强大，更多功能请参阅[Filters](#filtering)。

### 数据格式举例 ###

最后，我们想对结果使用[CSV表示](https://sampleapp.fili.io/v1/data/network/week?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08&filters=productRegion|id-notin[Americas Region]&format=csv)，
把结果可以放进Excel做下一步处理。Fili API则完全支持CSV，

    GET https://sampleapp.fili.io/v1/data/network/week?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-09-01/2014-09-08&filters=productRegion|id-notin[Americas Region]&format=csv

Fili支持其他更多的表示格式，详见[格式](#response-format)单元！

访问请求选项
-------------

Fili API提供的数据支持很多访问选项：

- [分页 / Limit](#pagination--limit)
- [Response Format](#response-format)
- [Filtering](#filtering)
- [Having](#having)
- [Dimension Field Selection](#dimension-field-selection)
- [Sorting](#sorting)

### Pagination / Limit ###

分页把数据结果分成多个页面，根据需求每次返回一个页面的数据。你不用一次性处理巨型的返回结果，可以用分页把结果分成很多页，
每一页包含一小部分数据，抽取某一页数据去处理就行。

目前，支持分页的有[维度](#dimensions)和[数据](#data-queries)访问接口。

每一页除了包含该页的实际数据，还加入了分页参数数据。维度和数据接口的分页参数暂时不同，
In addition to containing only the desired page of results, the response also contains pagination metadata. Currently, 
the dimension and data endpoints show different metadata, but there are plans to have the dimension endpoint display
the same kind of metadata as the data endpoint.

To paginate a resource that supports it, there are two query parameters at your disposal:

- **perPage**: How many result rows/resources to have on a page. Takes a positive integer as a parameter.

- **page**: Which page to return, with `perPage` rows per page. Takes a positive integer as a parameter.

With these two parameters, we can, for example, get the [2nd page with 3 records per page](https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=2):

    GET https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=2
   
With all response formats, a `link` header is added to the HTTP response. These are links to the first, last, next, and
previous pages with `rel` attributes `first`, `last`, `next`, and `prev` respectively. If we use our [previous example](https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=2),    
the link header in the response would be:
    
     Link: 
        https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=1; rel="first",
        https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=3; rel="last"
        https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=3; rel="next",
        https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=1; rel="prev",

There are, however, a few differences between pagination for Dimension and Data endpoints:

#### Data #####
    
For JSON (and JSON-API) responses, a `meta` object is included in the body of the response:
 
```json
"meta": {
    "pagination": {
        "currentPage": 2,
        "rowsPerPage": 3,
        "numberOfResults": 7
        "first": "https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=1",
        "previous": "https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=1",
        "next": "https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=3",
        "last": "https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=3"
    }
}
```

The `meta` object contains a `pagination` object which contains links to the `first`, `last`, `next` and `previous`
pages. The `meta` object contains other information as well:

- `currentPage` : The page currently being displayed
- `rowsPerPage` : The number of rows on each page
- `numberOfResults` : The total number of rows in the entire result

_Note: For the data endpoint, **both** the `perPage` and `page` parameters must be provided. The data endpoint has no 
default pagination._

##### Pagination Links #####

When paginating, the `first` and `last` links will always be present, but the `next` and `previous` links will only be
included in the response if there is at least 1 page either after or before the requested page. Or, to put it another
way, the response for the [1st page](https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=1)
won't include a link to the `previous` page, and the response for the [last page](https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews&dateTime=2014-09-01/2014-09-08&perPage=3&page=3)
won't include a link to the `next` page.

#### Dimension ####

Currently, the dimension endpoint only prints the `previous` and `next` links inside the top-level JSON object. It does, 
however, include the same links in the headers as the data endpoint: `first`, `last`, `next` and `prev`.

Unlike the Data endpoint, the Dimension endpoint _always_ paginates. It defaults to page 1, and 10000 rows per page. The
default rows per page is configurable, and may be adjusted by modifying the configuration `default_per_page.` 

_Note that `default_per_page` applies **only** to the Dimension endpoint. It does not affect the Data endpoint._

- **perPage**:
    Setting only the `perPage` parameter also gives a "limit" behavior, returning only the top `perPage` rows.

    [Example](https://sampleapp.fili.io/v1/dimensions/productRegion/values?perPage=2): `GET https://sampleapp.fili.io/v1/dimensions/productRegion/values?perPage=2`
    
    _Note: This will likely change to not return "all" by default in a future version_
    
- **page**:    
    `page` defaults to 1, the first page.

    Note: In order to use `page`, the `perPage` query parameter must also be set.

    [Example](https://sampleapp.fili.io/v1/dimensions/productRegion/values?perPage=2&page=2): `GET https://sampleapp.fili.io/v1/dimensions/productRegion/values?perPage=2&page=2`

### 数据格式 ###

部分访问资源支持不同的数据格式。默认的格式是JSON，某些资源也支持CSV和JSON-API格式。

如果需要配置数据格式，请在查询语句中加入`format`变量。

[JSON](https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&format=json): `GET https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&format=json`

```json
{
    "rows": [
        {
            "dateTime": "2014-09-01 00:00:00.000",
            "gender|id": "-1",
            "gender|desc": "Unknown",
            "pageViews": 1681441753
        },{
            "dateTime": "2014-09-01 00:00:00.000",
            "gender|id": "f",
            "gender|desc": "Female",
            "pageViews": 958894425
        },{
            "dateTime": "2014-09-01 00:00:00.000",
            "gender|id": "m",
            "gender|desc": "Male",
            "pageViews": 1304365910
        }
    ]
}
```

[CSV](https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&format=csv): `GET https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&format=csv`

```csv
dateTime,gender|id,gender|desc,pageViews
2014-09-01 00:00:00.000,-1,Unknown,1681441753
2014-09-01 00:00:00.000,f,Female,958894425
2014-09-01 00:00:00.000,m,Male,1304365910
```

[JSON-API](https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&format=jsonapi): `GET https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&format=jsonapi`

```json
{
    "rows": [
        {
            "dateTime": "2014-09-01 00:00:00.000",
            "gender": "-1",
            "pageViews": 1681441753
        },{
            "dateTime": "2014-09-01 00:00:00.000",
            "gender": "f",
            "pageViews": 958894425
        },{
            "dateTime": "2014-09-01 00:00:00.000",
            "gender": "m",
            "pageViews": 1304365910
        }
    ],
    "gender": [
        {
            "id": "-1",
            "desc": "Unknown"
        },{
            "id": "f",
            "desc": "Female"
        },{
            "id": "m",
            "desc": "Male"
        }
    ]
}
```

### 筛选 ###

你可以根据[维度](#dimension)值去筛选数据。不同的数据有不同的筛选结果，但是筛选的基本用法和原理都是一样的。

筛选的一般格式是：

    dimensionName|dimensionField-filterOperation[some,list,of,url,encoded,filter,strings]

多个筛选项用逗号隔开，筛选采用[URL编码](http://en.wikipedia.org/wiki/Percent-encoding)，筛选值用逗号隔开：

    myDim|id-contains[foo,bar],myDim|id-notin[baz],yourDim|desc-startsWith[Once%20upon%20a%20time,in%20a%20galaxy]

支持的所有filter operations为（取决于不同接口）:

- **in**: `In`筛选是一个完全匹配项-只有在筛选项里列出的数据列才会被选中返回
- **notin**: `Not In`筛选也是一个完全匹配项-只有_没有_在筛选项里列出的数据列才会被选中返回
- **contains**: `Contains`筛选出值包含在指令内容中的数据，类似于`in`筛选
- **startsWith**: `Starts With`筛选出值以制定内容其实的数据，类似于`in`筛选

举个例子解释一下。

[比如](https://sampleapp.fili.io/v1/dimensions/productRegion/values?filters=productRegion|id-notin[Americas%20Region,Europe%20Region],productRegion|desc-contains[Region]):

    GET https://sampleapp.fili.io/v1/dimensions/productRegion/values?filters=productRegion|id-notin[Americas%20Region,Europe%20Region],productRegion|desc-contains[Region]

筛选的含义是：

    返回满足以下要求的维度数值：
        不包含
            ID是"Americas Region"或"Europe Region"的productRegion维度值
        包含
            描述语句包含"Region"单词的productRegion维度值


### Having ###

Having clauses allow you to to filter out result rows based on conditions on aggregated metrics. 
This is similar to, but distinct from [Filtering](#filtering), which allows you to filter out results based on 
dimensions. As a result, the format for writing a having clause is very similar to that of a filter. 

The general format of a single having clause is:

    metricName-operator[x,y,z]
    
where the parameters `x, y, z` are numbers (integer or float) in decimal (`3, 3.14159`) or scientific (`4e8`) notation. 
Although three numbers are used in the template above, the list of parameters may be of any length, but must be 
non-empty.

These clauses can be combined by comma-separating individual clauses:

    metricName1-greaterThan[w,x],metricName2-equals[y],metricName3-notLessThan[y, z]
  
which is read as _return all rows such that metricName1 is greater than w or x, and metricName2 is
equal to y, and metricName3 is less than neither y nor z_.

Note that you may only perform a having filter on metrics that have been requested in the `metrics` clause.

Following are the available having operators. Each operator has an associated shorthand. The shorthand is indicated
in parenthesis after the name of the operator. Both the full name and the shorthand may be used in a query.

- **equal(eq)**: `Equal` returns rows whose having-metric is equal to at least one of the specified values.
- **greaterThan(gt)**: `Greater Than` returns rows whose having-metric is strictly greater than at least one of the
specified values.
- **lessThan(lt)**: `Less Than` returns rows whose having-metric is strictly less than at least one of the specified 
values.

Each operation may also be prefixed with `not`, which negates the operation. So `noteq` returns all the rows whose
having-metric is equal to _none_ of the specified values.

Let's take [an example](https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews,users&dateTime=2014-09-01/2014-09-08&having=pageViews-notgt[4e9],users-lt[1e8]) and break down what it means.

    GET https://sampleapp.fili.io/v1/data/network/day?metrics=pageViews,users&dateTime=2014-09-01/2014-09-08&having=pageViews-notgt[4e9],users-lt[1e8]

What this having clause means is the following: 

    Return the page views and users of all events aggregated at the day level from September 1 to 
    September 8, 2014 that
         have at most 400 million page views
         and 
         have strictly more than 100 million users
         
#### Caveats ####         

The having filter is only applied at the Druid level. Therefore, the results of a having filter are not guaranteed to
be accurate if Fili performs any post-Druid calculations on one of the metrics being filtered on.
         
### Dimension Field Selection ###

By default, a query's response will return the id and description for each dimension in the request. However, you may be
interested in more information about the dimensions, or less. To do this, you can [specify a `show` clause on the
relevant dimension path segment](https://sampleapp.fili.io/v1/data/network/week/productRegion;show=desc/userCountry;show=id,regionId/?metrics=pageViews&dateTime=2014-09-01/2014-09-08):

    GET https://sampleapp.fili.io/v1/data/network/week/productRegion;show=desc/userCountry;show=id,regionId/?metrics=pageViews&dateTime=2014-09-01/2014-09-08

The results for this query will only show the description field for the Product Region dimension, and both the id and 
region id fields for the User Country dimension. In general you add `show` to a dimension with a semicolon, then
`show=<fieldnames>`. Use commas to separate in multiple fields in the same show clause:

    /<dimension>;show=<field>,<field>,<field>

#### Field selection keywords ####

There are a couple of keywords that can be used when selecting fields to `show`:

- **All**: Include all fields for the dimension in the response
- **None**: Include only the key field in the dimension. 

The `none` keyword also simplifies the response to keep the size as small as possible. The simplifications applied to
the response depend on the format of the response:

##### JSON #####
Instead of the normal format for each requested field for a dimension (`"dimensionName|fieldName":"fieldValue"`), each 
record in the response will only have a single entry for the dimension who's value is the value of the key-field for
that dimension (`"dimensionName":"keyFieldValue"`)

##### CSV #####
Instead of the normal header format for each requested field for a dimension (`"dimensionName|fieldName":"fieldValue"`),
the headers of the response will only have a single entry for the dimension, which will be the dimension's name. The
values of the column for that dimension will be the key-field for that dimension.

##### JSON-API #####
The `none` keyword for a dimension prevents the sidecar object for that dimension from being included in the response.

            
### Sorting ###

Sorting of the records in a response [can be done](https://sampleapp.fili.io/v1/data/network/day/gender?metrics=pageViews&dateTime=2014-09-01/2014-09-02&sort=pageViews|asc)
using the `sort` query parameter like so: 

    sort=myMetric

By default, sorting is _descending_, however Fili supports sorting in both descending or ascending order. To specify the
sort direction for a metric, you need to specify both the metric you want to sort on, as well as the direction, 
separated by a pipe (`|`) like so:

    sort=myMetric|asc

Sorting by multiple metrics, with a mixture of ascending and descending, can also be done by separating each sort with a
comma:

    sort=metric1|asc,metric2|desc,metric3|desc

#### Caveats ####         

There are, however, a few catches to this:

- Only the Data resource supports sorting
- Records are always sorted by `dateTime` first, and then by any sorts specified in the query, so records are always
  sorted _within_ a timestamp
- Sort is only supported on Metrics
- Sorting is only applied at the Druid level. Therefore, the results of a sort are not guaranteed to be accurate if Fili 
performs any post-Druid calculations on one of the metrics that you are sorting on.

### TopN ###

Suppose we would like to know which three pages have the top three pageview counts for each week between January and 
September 2014. We can easily answer such a question with a `topN` query. `topN` queries allow us to ask for the top 
results for each time bucket, up to a limit `n` in a request. Of course, a `topN` query implies that some sort of ordering has been imposed
 on the data. Therefore, a `topN` query has two components:

1. `topN=n` where `n` is the number of results to return for each bucket
2. `sort=metricName|dir` telling Fili how to sort the results before filtering down to the top N. See the section on 
[sorting](#sorting) for more details about the sort clause.

Going back to the sample question at the start of the section, let's see how that looks as a
[Fili query](https://sampleapp.fili.io/v1/data/network/week/pages?metrics=pageViews&dateTime=2014-06-01/2014-08-31&topN=3&sort=pageViews|desc):

    GET https://sampleapp.fili.io/v1/data/network/week/pages?metrics=pageViews&dateTime=2014-06-01/2014-08-31&topN=3&sort=pageViews|desc

We want the three highest pageview counts for each week, so `n` is set to three, and the query is aggregated to the week
granularity. Furthermore, we want the three _largest_ pagecounts. Therefore, we sort `pageViews` in descending order (the
first entry is the highest, the second entry is the lowest, and so on).

Fili also supports asking for metrics in addition to the one being sorted on in `topN` queries. Suppose we want to know the [daily average time 
spent (`dayAvgTimeSpent`) on the three pages with the highest number of page views for each week between January 6th and September 
1st](https://sampleapp.fili.io/v1/data/network/week/pages?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-06-01/2014-08-31&topN=3&sort=pageViews|desc).

Thus, we can add `dayAvgTimeSpent` to our original `topN` query:

    GET https://sampleapp.fili.io/v1/data/network/week/pages?metrics=pageViews,dayAvgTimeSpent&dateTime=2014-06-01/2014-08-31&topN=3&sort=pageViews|desc

#### Caveats ####

When executing a `topN` query with multiple metrics, Fili will compute the top N results using the sorted metric _only_.

Remember that `topN` provides the top N results _for each time bucket_. Therefore, when we ask a `topN` query, we will
get `n * numBuckets` results. In both of the examples above, we would get `3 * 34 = 102` results. If you are only
interested in the first `n` results, see [pagination/limit](#pagination--limit).

Asynchronous Queries
--------------------
Fili supports asynchronous data queries. A new parameter `asyncAfter` is added on data queries. The `asyncAfter`
parameter will control whether a data query should always be synchronous, or transition from synchronous to asynchronous
 on the fly. If `asyncAfter=never` then Fili will wait indefinitely for the data, and hold the connection with the
 client open as long as allowed by the network. This will be the default. However, the default behavior of `asyncAfter`
 may be modified by setting the `default_asyncAfter` configuration parameter. If `asyncAfter=always`, the query is
 asynchronous immediately. If `asyncAfter=t` for some positive integer `t`, then _at least_ `t` milliseconds will pass
 before the query becomes asynchronous. Note however that the timing is best effort. The query may take longer than
 `t` milliseconds and still be synchronous. In other words, `asyncAfter=0` and `asyncAfter=always` do not mean the same
 thing. It is possible for `asyncAfter=0` to return the query results synchronously (this may happen if the results come
 back sufficiently fast). It is _impossible_ for the query results to return synchronously if `asyncAfter=always`.

If the timeout passes, and the data has not come back, then the user receives a `202 Accepted` response and the
[job meta-data](#job-meta-data).


### Jobs Endpoint
The jobs endpoint is the one stop shop for queries about asynchronous jobs. This endpoint is responsible for:

1. Providing a list of [all jobs](#get-ting-summary-of-all-jobs) in the system.
2. Providing the status of a [particular job](#get-ting-job-status) queried via the `jobs/TICKET` resource.
3. Providing access to the [results](#get-ting-results) via the `jobs/TICKET/results` resource.

#### GET-ting summary of all jobs
A user may get the status of all jobs by sending a `GET` to `jobs` endpoint.

```
https://HOST:PORT/v1/jobs
```
If no jobs are available in the system, an empty collection is returned.

The `jobs` endpoint supports filtering on job fields (i.e. `userId`, `status`), using the same syntax as the
[data endpoint filters](#filtering). For example:

`userId-eq[greg, joan], status-eq[success]`

resolves into the following boolean operation:

`(userId = greg OR userId = joan) AND status = success`

which will return only those Jobs created by `greg` and `joan` that have completed successfully.

#### GET-ting Job Status
When the user sends a `GET` request to `jobs/TICKET`, Fili will look up the specified ticket and return the job's
meta-data as follows:

###### Job meta-data
```json
{
    "query": "https://HOST:PORT/v1/data/QUERY",
    "results": "https://HOST:PORT/v1/jobs/TICKET/results",
    "syncResults": "https://HOST:PORT/v1/jobs/TICKET/results?asyncAfter=never",
    "self": "https://HOST:PORT/v1/jobs/TICKET",
    "status": ONE OF ["pending", "success", "error"],
    "jobTicket": "TICKET",
    "dateCreated": "ISO 8601 DATETIME",
    "dateUpdated": "ISO 8601 DATETIME",
    "userId": "Foo"
}
```
* `query` is the original query
* `results` provides a link to the data, whether it is fully synchronous or switches from
   synchronous to asynchronous after a timeout depends on the default setting of `asyncAfter`.
* `syncResults` provides a synchronous link to the data (note the `asyncAfter=never` parameter)
* `self` provides a link that returns the most up-to-date version of this job
* `status` indicates the status of the results pointed to by this ticket
    - `pending` - The job is being worked on
    - `success` - The job has been completed successfully
    - `error` - The job failed with an error
    - `canceled` - The job was canceled by the user (coming soon)
* `jobTicket` is a unique identifier for the job
* `dateCreated` is the date on which the job was created
* `dateUpdated` when the job's status was last updated
* `userId` is an identifier for the user who submitted this job

If the ticket is not available in the system, we get a 404 error with the message `No job found with job ticket TICKET`

#### GET-ting Results
The user may access the results of a query by sending a `GET` request to `jobs/TICKET/results`. This
resource takes the following parameters:

 1. **`format`** - Allows the user to specify a response format, i.e. csv, or JSON. This behaves
just like the [`format`](#response-format) parameter on queries sent to the `data` endpoint.

2. **`page`, `perPage`** - The [pagination](#pagination--limit) parameters. Their behavior is the same as when sending
a query to the `data` endpoint, and allow the user to get pages of the results.

3. **`asyncAfter`** - Allows the user to specify how long they are willing to wait for results from the
result store. Behaves like the [`asyncAfter`](async) parameter on the `data` endpoint.

If the results for the given ticket are ready, we get the results in the format specified. Otherwise, we get the
[job's metadata](#job-meta-data).

##### Long Polling
If clients wish to long poll for the results, they may send a `GET` request to
`https://HOST:PORT/v1/jobs/TICKET/results?asyncAfter=never` (the query linked to under the
`syncResults` field in the async response). This request will perform like a synchronous query: Fili
will not send a response until all of the data is ready.

Misc
----

### Dates and Times ###

The date interval is specified using the `dateTime` parameter in the format `dateTime=d1/d2`. The first date is the start date, and the second is the non-inclusive end date. For example, `dateTime=2015-10-01/2015-10-03` will return the data for October 1st and 2nd, but not the 3rd. Dates can be one of: 

1. ISO 8601 formatted date
2. ISO 8601 duration  (see below)
3. Date macro (see below)

We have followed the ISO 8601 standards as closely as possible in the API. Wikipedia has a [great article](http://en.wikipedia.org/wiki/ISO_8601) on ISO 8601 dates and times if you want to dig deep. 

#### Date Periods  (ISO 8601 Durations) ####

Date Periods have been implemented in accordance with the [ISO 8601 standard](https://en.wikipedia.org/wiki/ISO_8601#Durations). Briefly, a period is specified by the letter `P`, followed by a number and then a timegrain (M=month,W=week,D=day,etc).  For example, if you wanted 30 days of data, you would specify `P30D`.  The number and period may be repeated, so `P1Y2M` is an interval of one year and two months. 

This period can take the place of either the start or end date in the query.

#### Date Macros ####

We have created a macro named `current`, which will always be translated to the beginning of the current time grain period.  For example, if your time grain is `day`, then `current` will resolve to today’s date.  If your query time grain is `month`, then `current` will resolve to the first of the current month.

There is also a similar macro named `next` which resolves to the beginning of the next interval. For example, if your time grain is `day`, then, `next` will resolve to tomorrow's date.

#### Time Zone

Currently, time zone cannot be specified for the start or stop instants of an interval. Instead, the time zone of a query can be changed via the `timeZone` query parameter. This changes the time zone in which the intervals specified in the `dateTime` are interpreted. By default, the query will use the default time zone of the API, but any [time zone identifier](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones#List) can be specified to override that. For example, specifying query parameters

    dateTime=2016-09-16/2016-09-17&timeZone=America/Los_Angeles
    vs
    dateTime=2016-09-16/2016-09-17&timeZone=America/Chicago

will result in the intervals resolving to

    2016-09-16 00:00:00-07:00/2016-09-17 00:00:00-07:00
    and
    2016-09-16 00:00:00-05:00/2016-09-17 00:00:00-05:00

<sub>Note the hour offsets on the interval instants.</sub>

### Case Sensitivity ###

Everything in the API is case-sensitive, which means `pageViews` is not the same as `pageviews` is not the same as 
`PaGeViEwS`.

### Rate Limiting ###

To prevent abuse of the system, the API only allows each user to have a certain number of data requests being processed
at any one time. If you try to make another request that would put you above the allowed limit, you will be given an
error response with an HTTP response status code of 429.  

### Errors ###

There are a number of different errors you may encounter when interacting with the API. All of them are indicated by the
HTTP response status code, and most of them have a helpful message indicating what went wrong.

#### 400 BAD REQUEST ####

You have some sort of syntax error in your request. We couldn't figure out what you were trying to ask.

#### 401 UNAUTHORIZED ####

We don't know who you are, so send your request again, but tell us who you are.

Usually this means you didn't include proper security authentication information in your request

#### 403 FORBIDDEN ####

We know who you are, but you can't come in.

#### 404 NOT FOUND ####

We don't know what resource you're talking about. You probably have a typo in your URL path.

#### 416 REQUESTED RANGE NOT SATISFIABLE ####

We can't get that data for you from Druid.

#### 422 UNPROCESSABLE ENTITY ####

We understood the request (ie. syntax is correct), but something else about the query is wrong. Likely something like
a dimension mis-match, or a metric / dimension not being available in the logical table.

#### 429 TOO MANY REQUESTS ####

You've hit the rate limit. Wait a little while for any requests you may have sent to finish and try your request again.

#### 500 INTERNAL SERVER ERROR ####

Some other error on our end. We try to catch and investigate all of these, but we might miss them, so please let us know
if you get 500 errors.

#### 502 BAD GATEWAY ####

Bad Druid response

#### 503 SERVICE UNAVAILABLE ####

Druid can't be reached

#### 504 GATEWAY TIMEOUT ####

Druid query timeout

#### 507 INSUFFICIENT STORAGE ####

Too heavy of a query
