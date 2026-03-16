<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html>
<head>
  <title>Customers Dashboard</title>

  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

  <style>

    body{
      background-color:#0f1419;
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

    .stat-card{
      color:#7CFC00;
      font-size:32px;
      font-weight:bold;
    }

  </style>

</head>

<body>

<div class="container mt-5">

  <h2 class="mb-4">Customers Dashboard</h2>

  <!-- TOTAL CARD -->

  <div class="row mb-4">

    <div class="col-md-4">

      <div class="card p-4">

        <h6 style="color:#888">TOTAL CUSTOMERS</h6>

        <div class="stat-card">
          ${totalCustomers}
        </div>

      </div>

    </div>

  </div>

  <!-- TABLE -->

  <div class="card">

    <div class="card-header">
      <h5>Latest 5 Customers</h5>
    </div>

    <div class="card-body">

      <table class="table">

        <thead>

        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Email</th>
          <th>Registered At</th>
        </tr>

        </thead>

        <tbody>

        <%
          List<Object[]> list = (List<Object[]>) request.getAttribute("customers");

          if(list!=null){
            for(Object[] row : list){
        %>

        <tr>
          <td><%= row[0] %></td>
          <td style="color:#7CFC00;"><%= row[1] %></td>
          <td><%= row[2] %></td>
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