
Array.prototype.contains = function ( needle ) {
    for (i in this) {
        if (this[i] == needle) return true;
    }
    return false;
}


function ciTopo(el,ciId,deep){
    var jsonUrl =  '/balantflow/module/balantcmdb/cmdb/getCITopologyJson.do?deep=' + deep + '&ownerId=' + ciId;
    d3.json(jsonUrl, function(error, jsonData) {
        if(jsonData == 'err'){
            showPopMsg("数据加载异常!","error");
        }else {
            paintTopo(el, jsonData,deep);
        }
    });
}


function paintTopo(el,json,deep){
    d3.select("svg").remove();
    var nodeSize = json.nodes.length ;
    var w, h,distance;
    w = 800;
    h = 500;
    if( nodeSize > 20 ){
        distance = 120;
    }else{
        distance = 180;
    }


    var svg = d3.select(el).append("svg:svg")
        .attr("width", w)
        .attr("height", h);

    // build the arrow.
    svg.append("svg:defs").selectAll("marker")
        .data(["end"])
        .enter().append("svg:marker")
        .attr("id", "idArrow")
        .attr("viewBox", "0 0 20 20")
        .attr("refX", 0)
        .attr("refY", 10)
        .attr("markerWidth", 5)
        .attr("markerHeight", 10)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M 0 0 L 20 10 L 0 20 z");


    //var vis = svg.append('svg:g');


    var force = d3.layout.force()
        .size([w,h])
        .gravity(.05)
        .linkDistance( distance )
        .charge(-200);

    force
        .nodes(json.nodes)
        .links(json.links);
    //.on("tick", tick)
    //.start();

    var drag = force.drag()
        .on("dragstart", dragstart);

    var link = svg.selectAll(".link")
            .data(force.links())
            .enter()
            .append("line")
            .attr("class", function(d){ return "link" + d.type; })
        ;



    var text = svg.selectAll('.link')
        .data(force.links())
        .enter().append('text')
        .text(function (d) {
            return d.linkName;
        })
        .style({opacity:'0.6',fill:'gray'});

    var node = svg.selectAll(".node")
        .data(force.nodes())
        .enter()
        .append("svg:g")
        .attr("class", "node")
        //.call(drag)
        .on("dblclick",function(d){
            ciTopo(el,d.ciId,deep);
        });



    node.append("image")
        .attr("xlink:href", function(d) {
            return d.iconUrl;
        })
        .attr("x", -14)
        .attr("y", -10)
        .attr("width", 30)
        .attr("height", 20);


    node.append("text")
        .attr("x", 15)
        .attr("y", ".4em")
        .attr("class","txt")
        .text(function(d) { return d.name; })
        .style("fill",function(d){
            if(d.alertStatus == 'critical'){
                return "red";
            }if(d.alertStatus == 'warning'){
                return "orange";
            }else{
                return "black";
            }
        });

    node.append("title")
        .text(function(d) {
            return  '[' + d.alertStatus + ']' + d.disc;
        });

    force.on("tick", function() {
        link

            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) {
                var deltaX = d.source.x > d.target.x ? ( d.source.x - d.target.x ) : ( d.target.x - d.source.x );
                if(d.source.x < d.target.x){
                    if( deltaX > 30){
                        return d.target.x - 15;
                    }else {
                        return d.target.x;
                    }
                }else{
                    if( deltaX > 30){
                        return d.target.x + 15;
                    }else {
                        return d.target.x;
                    }
                }

            })
            .attr("y2", function(d) {
                var deltaY = d.source.y > d.target.y ? ( d.source.y - d.target.y ) : ( d.target.y - d.source.y );
                if(d.source.y < d.target.y){
                    if( deltaY > 30){
                        return d.target.y - 15;
                    }else {
                        return d.target.y;
                    }
                }else{
                    if( deltaY > 30){
                        return d.target.y + 15;
                    }else {
                        return d.target.y;
                    }
                }

            });

        node.attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")";
        });

        text.attr('transform', function (d) {
            return 'translate(' + getMiddlePointX(d) + ',' + getMiddlePointY(d) + ')';
        });

    });



    setTimeout(function() {

        //Todo 计算深度
        //var nodeLinks = json.links;
        //forEach(json.links)
        var n ;
        if(nodeSize < 20){
            n = 20;
        }else{
            n = 30;
        }
        force.start();
        for (var i = n * n; i > 0; --i) {
            force.tick();
        }
        force.stop();
        link.attr("marker-end", 'url(#idArrow)');
        if( window.navigator.userAgent.indexOf("Trident") == -1 ) {
            node.call(drag);
        }else{
            console.info("sorry...drag not support in ie.");
        }
    },100);

}

function getMiddlePointX(d){
    if( d.source.x > d.target.x){
        return d.target.x + ( d.source.x - d.target.x)/2 - 20;
    }else{
        return d.source.x + ( d.target.x - d.source.x)/2 - 20;
    }
}

function getMiddlePointY(d){
    if( d.source.y > d.target.y){
        return d.target.y + ( d.source.y - d.target.y)/2;
    }else{
        return d.source.y + ( d.target.y - d.source.y)/2;
    }
}

function dragstart(d) {
    d.fixed = true;
    d3.select(this).classed("fixed", true);
}

function click(d){
    if (!d3.event.defaultPrevented) {
        //console.info(d);
        d3.select(this).select("text").transition()
            .duration(750)
            .attr("x", 22)
            .style("fill", "steelblue")
            .style("stroke", "lightsteelblue")
            .style("stroke-width", ".5px")
            .style("font", "20px sans-serif");
    }
}
