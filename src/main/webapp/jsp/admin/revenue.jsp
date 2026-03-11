<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html>
<head>
    <title>Revenue Dashboard</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>

        body{
            background:#0f1419;
            color:#e6e6e6;
        }

        .card{
            background:#151a21;
            border:none;
            border-radius:12px;
        }

        .card-header{
            background:#151a21;
            border-bottom:1px solid #222;
        }

        .table{
            color:#ddd;
        }

        .table thead{
            background:#1f252d;
        }

        .table tbody tr:hover{
            background:#1b2128;
        }

        .revenue-number{
            color:#7CFC00;
            font-size:32px;
            font-weight:bold;
        }

        .section-title{
            color:#aaa;
            font-size:14px;
        }

    </style>

</head>

<body>

<div class="container mt-5">

    <h2 class="mb-4">Revenue Dashboard</h2>

    <!-- TOTAL REVENUE -->

    <div class="row mb-4">

        <div class="col-md-4">

            <div class="card p-4">

                <div class="section-title">TOTAL REVENUE</div>

                <div class="revenue-number">
                    ${totalRevenue} VND
                </div>

            </div>

        </div>

    </div>

    <!-- TRANSACTIONS -->

    <div class="card">

        <div class="card-header">
            <h5>Recent Transactions</h5>
        </div>

        <div class="card-body">

            <table class="table">

                <thead>

                <tr>
                    <th>Payment ID</th>
                    <th>Customer</th>
                    <th>Amount</th>
                    <th>Date</th>
                </tr>

                </thead>

                <tbody>

                <%
                    List<Object[]> list = (List<Object[]>) request.getAttribute("transactions");

                    if(list != null){
                        for(Object[] row : list){
                %>

                <tr>
                    <td><%= row[0] %></td>
                    <td style="color:#7CFC00;"><%= row[1] %></td>
                    <td class="fw-bold"><%= row[2] %> VND</td>
                    <td><%= row[3] %></td>
                </tr>

                <%
                        }
                    }
                %>

                </tbody>

            </table>

        </div>

    </div>

</div>

</body>
</html>