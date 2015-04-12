/**
 * Created by keen on 4/12/15.
 */
function currencyGraph(jsonList) {
    el = document.getElementById("minMonthSelect");
    minMonth= el.options[el.selectedIndex].value;
    el = document.getElementById("minYearSelect");
    minYear= el.options[el.selectedIndex].value;
    el = document.getElementById("maxMonthSelect");
    maxMonth= el.options[el.selectedIndex].value;
    el = document.getElementById("maxYearSelect");
    maxYear= el.options[el.selectedIndex].value;

    el = document.getElementById("wrong");
    if(minYear > maxYear || (minYear == maxYear && minMonth >= maxMonth)) {
        el.innerHTML = "Error. Invalid date ranges selected.";
        return;
    }
    else
        el.innerHTML = "";

    var data=filterByDates(jsonList);

    var margin = {top: 30, right: 20, bottom: 40, left: 50},
        width = 1000 - margin.left - margin.right,
        height = 470 - margin.top - margin.bottom;

    var parseDateFormat = d3.time.format("%Y-%m");

    var x = d3.time.scale().range([0, width]);
    var y = d3.scale.linear().range([height, 0]);

    var xAxis = d3.svg.axis().scale(x)
        .orient("bottom").ticks(10);

    var yAxis = d3.svg.axis().scale(y)
        .orient("left").ticks(5);

    var valueline = d3.svg.line()
        .x(function(d) { return x(d.date); })
        .y(function(d) { return y(d.value); });

        d3.select("g").remove();

    var svg = d3.select("svg")
        //.append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");




    d3.select("table").remove();
    // render the table
    var peopleTable = tabulate(data, ["date", "value"]);

    peopleTable.selectAll("tbody tr")
        .sort(function(a, b) {
            return d3.descending(a.date, b.date);
        });

    peopleTable.selectAll("thead th")
        .text(function(column) {
            return column.charAt(0).toUpperCase()+column.substr(1);
        });



    data.forEach(function(d) {
        d.date = parseDateFormat.parse(d.date);
    });

    // Scale the range of the data
    x.domain(d3.extent(data, function(d) { return d.date; }));
    minValue=d3.min(data,function(d){return d.value;});
    maxValue=d3.max(data, function(d) { return d.value;});
    y.domain([minValue-(maxValue-minValue)/20, maxValue]);

    svg.append("path")      // Add the valueline path.
        .attr("class", "line")
        .attr("d", valueline(data));

    svg.append("g")         // Add the X Axis
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    // Add the text label for the x axis
    svg.append("text")
        .attr("transform", "translate(" + (width / 2) + " ," + (height + margin.bottom) + ")")
        .style("text-anchor", "middle")
        .text("Date");

    svg.append("g")         // Add the Y Axis
        .attr("class", "y axis")
        .call(yAxis);

    // Add the text label for the Y axis
    svg.append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 0 - margin.left)
        .attr("x",0 - (height / 2))
        .attr("dy", "1em")
        .style("text-anchor", "middle")
        .text("Value");


}


// The table generation function
function tabulate(data, columns) {
    var table = d3.select("body").append("table")
            .attr("style", "margin-left: auto")
            .attr("class","table table-bordered table-hover")
        thead = table.append("thead"),
        tbody = table.append("tbody");

    // append the header row
    thead.append("tr")
        .selectAll("th")
        .data(columns)
        .enter()
        .append("th")
        .text(function(column) { return column; });

    // create a row for each object in the data
    var rows = tbody.selectAll("tr")
        .data(data)
        .enter()
        .append("tr");

    // create a cell in each row for each column
    var cells = rows.selectAll("td")
        .data(function(row) {
            return columns.map(function(column) {
                return {column: column, value: row[column]};
            });
        })
        .enter()
        .append("td")
        .attr("style", "font-family: Courier") // sets the font style
        .html(function(d) { return d.value; });

    return table;
}





function filterByDates(jsonList){
    var data = [];
    for(i = 0; i < jsonList.length; ++i)
    {
        if(jsonList[i].date.substring(0, 4) > minYear || (jsonList[i].date.substring(0, 4) == minYear && jsonList[i].date.substring(5, 7) >= minMonth))
        {
            x = 0;
            for(j = i; j < jsonList.length; ++j)
            {
                if(jsonList[j].date.substring(0, 4) > maxYear || (jsonList[j].date.substring(0, 4) == maxYear && jsonList[j].date.substring(5, 7) > maxMonth))
                {
                    break;
                }
                else
                {
                    data[x++] = jsonList[j];
                }
            }
            break;
        }
    }
    return data;
}