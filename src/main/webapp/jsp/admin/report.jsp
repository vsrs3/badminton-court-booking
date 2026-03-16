<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ include file="layout/layout.jsp"%>
<%@ include file="layout/sidebar.jsp"%>

<div class="main-content">

    <%@ include file="layout/header.jsp"%>

    <div class="content-area">

        <!-- HEADER -->

        <div class="d-flex align-items-center justify-content-between mb-4">

            <div>
                <h1 class="fw-bold mb-1" style="font-size:1.7rem;">Report</h1>
                <p class="text-secondary mb-0">Performance summary of your badminton courts</p>
            </div>

            <a href="${pageContext.request.contextPath}/admin/export"
               class="btn btn-success rounded-3">
                Export Data
            </a>

        </div>


        <!-- ===== DASHBOARD CARDS ===== -->

        <div class="row g-4 mb-4">

            <!-- TOTAL REVENUE -->
            <div class="col-xl-3 col-md-6">

                <a href="${pageContext.request.contextPath}/admin/revenue" class="text-decoration-none">

                    <div class="card dashboard-card h-100 border-0 shadow-sm rounded-4">

                        <div class="card-body d-flex justify-content-between align-items-center">

                            <div>
                                <p class="text-uppercase text-muted small mb-1">Total Revenue</p>
                                <h3 class="fw-bold text-success mb-0">${totalRevenue} VND</h3>
                            </div>

                            <i class="bi bi-cash-stack dashboard-icon text-success"></i>

                        </div>

                    </div>

                </a>

            </div>



            <!-- TOTAL BOOKINGS -->

            <div class="col-xl-3 col-md-6">

                <a href="${pageContext.request.contextPath}/admin/bookings" class="text-decoration-none">

                    <div class="card dashboard-card h-100 border-0 shadow-sm rounded-4">

                        <div class="card-body d-flex justify-content-between align-items-center">

                            <div>
                                <p class="text-uppercase text-muted small mb-1">Total Bookings</p>
                                <h3 class="fw-bold text-primary mb-0">${totalBookings}</h3>
                            </div>

                            <i class="bi bi-calendar-check dashboard-icon text-primary"></i>

                        </div>

                    </div>

                </a>

            </div>



            <!-- UTILIZATION -->

            <div class="col-xl-3 col-md-6">

                <a href="${pageContext.request.contextPath}/admin/utilization" class="text-decoration-none">

                    <div class="card dashboard-card h-100 border-0 shadow-sm rounded-4">

                        <div class="card-body d-flex justify-content-between align-items-center">

                            <div>
                                <p class="text-uppercase text-muted small mb-1">Average Utilization</p>
                                <h3 class="fw-bold text-warning mb-0">${avgUtilization}%</h3>
                            </div>

                            <i class="bi bi-graph-up dashboard-icon text-warning"></i>

                        </div>

                    </div>

                </a>

            </div>



            <!-- NEW CUSTOMERS -->

            <div class="col-xl-3 col-md-6">

                <a href="${pageContext.request.contextPath}/admin/customers" class="text-decoration-none">

                    <div class="card dashboard-card h-100 border-0 shadow-sm rounded-4">

                        <div class="card-body d-flex justify-content-between align-items-center">

                            <div>
                                <p class="text-uppercase text-muted small mb-1">New Customers</p>
                                <h3 class="fw-bold text-danger mb-0">${newCustomers}</h3>
                            </div>

                            <i class="bi bi-person-plus dashboard-icon text-danger"></i>

                        </div>

                    </div>

                </a>

            </div>

        </div>



        <!-- ===== CHARTS ===== -->

        <div class="row g-4 mb-4">

            <!-- REVENUE CHART -->

            <div class="col-lg-6">

                <div class="card border-0 shadow-sm rounded-4 h-100">

                    <div class="card-body">

                        <h6 class="text-uppercase text-muted mb-3">Revenue Trends</h6>

                        <div style="height:250px">
                            <canvas id="revenueChart"></canvas>
                        </div>

                    </div>

                </div>

            </div>



            <!-- BOOKINGS CHART -->

            <div class="col-lg-6">

                <div class="card border-0 shadow-sm rounded-4 h-100">

                    <div class="card-body">

                        <h6 class="text-uppercase text-muted mb-3">Daily Bookings</h6>

                        <div style="height:250px">
                            <canvas id="bookingsChart"></canvas>
                        </div>

                    </div>

                </div>

            </div>

        </div>



        <!-- ===== QUICK ACTIONS ===== -->

        <div class="card border-0 shadow-sm rounded-4">

            <div class="card-body">

                <h6 class="text-uppercase text-muted mb-3">Quick Actions</h6>

                <a href="${pageContext.request.contextPath}/admin/accounts/list"
                   class="btn btn-success me-2">
                    Manage Account
                </a>

                <a href="${pageContext.request.contextPath}/owner/settings"
                   class="btn btn-outline-secondary">
                    System Settings
                </a>

            </div>

        </div>

    </div>

</div>



<!-- ===== CHART JS ===== -->

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<script>

    const days = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];

    new Chart(document.getElementById('revenueChart'),{

        type:'line',

        data:{
            labels:days,
            datasets:[{
                data:[2800,2600,2200,3100,4800,6300,5900],
                borderColor:'#22c55e',
                backgroundColor:'rgba(34,197,94,0.15)',
                fill:true,
                tension:0.4
            }]
        },

        options:{
            responsive:true,
            maintainAspectRatio:false,
            plugins:{legend:{display:false}}
        }

    });


    new Chart(document.getElementById('bookingsChart'),{

        type:'bar',

        data:{
            labels:days,
            datasets:[{
                data:[40,50,38,72,90,125,110],
                backgroundColor:'#84CC16'
            }]
        },

        options:{
            responsive:true,
            maintainAspectRatio:false,
            plugins:{legend:{display:false}}
        }

    });

</script>



<style>

    .dashboard-card{
        transition:all .25s ease;
        cursor:pointer;
        min-height:110px;
    }

    .dashboard-card:hover{
        transform:translateY(-5px);
        box-shadow:0 15px 35px rgba(0,0,0,0.15);
    }

    .dashboard-icon{
        font-size:2rem;
        opacity:0.85;
    }

</style>