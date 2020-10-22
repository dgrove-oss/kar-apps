import { Component, OnInit, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { Port } from 'src/app/core/models/port';
import { MatDialog } from '@angular/material/dialog';
import { RestService } from 'src/app/core/services/rest.service';
import { MatTableDataSource } from '@angular/material/table';
import { SelectionModel } from '@angular/cdk/collections';
import { MatPaginator } from '@angular/material/paginator';
import { Reefer } from 'src/app/core/models/reefer';
import { MatSort } from '@angular/material/sort';
import { SocketService } from 'src/app/core/services/socket.service';
import { ReeferStats } from 'src/app/core/models/reefer-stats';

@Component({
  selector: 'app-reefer-view',
  templateUrl: './reefer-view.component.html',
  styleUrls: ['./reefer-view.component.scss']
})
export class ReeferViewComponent implements OnInit {
  selectedPort : string;
  ports: Port[] = [];
  reefers: Reefer[] = [];
  reeferStats: ReeferStats;
  portsDataSource = new MatTableDataSource(this.ports);
  reeferDataSource = new MatTableDataSource(this.reefers);
  portTableColumns: string[] = [ 'port', 'position', 'reeferCnt', 'onMaintenance', 'add'];
  reeferTableColumns: string[] = [ 'reeferId','port',  'position', 'maxCapacity', 'freeCapacity', 'status', 'loadingStatus'];
  filterValues = {};
  filterSelectObj = [];
  messageList:  string[] = [];
  portSelection = new SelectionModel<Port>(false, []);
  reeferSelection = new SelectionModel<Reefer>(false, []);
  totalReefers : number ;
  totalInTransitReefers : number;
  totalBookedReefers : number;
  totalSpoiltReefers : number;
  totalOnMaintenanceReefers : number;
  createAnomalyManually: boolean;
  failureRate: number;
  updateFrequency : number;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;


  constructor(private dialog: MatDialog, private restService: RestService, private webSocketService : SocketService ) {
    this.restService.getReeferControls().subscribe((data) => {
       this.failureRate = data.failureRate;
      this.updateFrequency = data.updateFrequency;

    });
    this.restService.getReeferStats().subscribe((data) => {
      this.totalReefers = data.total;
      this.totalBookedReefers = data.totalBooked;
      this.totalInTransitReefers = data.totalInTransit;
      this.totalSpoiltReefers = data.totalSpoilt;
      this.totalOnMaintenanceReefers = data.totalOnMaintenance;
      });
// Object to create Filter for
		// Open connection with server socket
    let stompClient = this.webSocketService.connect();
    stompClient.connect({}, frame => {
      console.log('ReeferView - connected socket');

        stompClient.subscribe('/topic/reefers/stats', (event:any) => {
          if ( event.body) {
            this.reeferStats = JSON.parse(event.body);
            //console.log('::::::'+this.reeferStats);
            this.totalReefers = this.reeferStats.total;
            this.totalBookedReefers = this.reeferStats.totalBooked;
            this.totalInTransitReefers = this.reeferStats.totalInTransit;
            this.totalSpoiltReefers = this.reeferStats.totalSpoilt;
            this.totalOnMaintenanceReefers = this.reeferStats.totalOnMaintenance;
          }

        })

    });

  }


  ngOnInit(): void {
    this.reeferDataSource.paginator = this.paginator;
    this.reeferDataSource.sort = this.sort;

  }

  public doFilter = (value: string) => {
    this.reeferDataSource.filter = value.trim().toLocaleLowerCase();
  }

  // Get Uniqu values from columns to build filter
  getFilterObject(fullObj, key) {
    const uniqChk = [];
    fullObj.filter((obj) => {
      if (!uniqChk.includes(obj[key])) {
        uniqChk.push(obj[key]);
      }
      return obj;
    });
    return uniqChk;
  }
  // Called on Filter change
  filterChange(filter, event) {
    this.filterValues[filter.columnProp] = event.target.value.trim().toLowerCase()
    this.reeferDataSource.filter = JSON.stringify(this.filterValues)
  }
// Custom filter method fot Angular Material Datatable
createFilter() {
  let filterFunction = function (data: any, filter: string): boolean {
    let searchTerms = JSON.parse(filter);
    let isFilterSet = false;
    for (const col in searchTerms) {
      if (searchTerms[col].toString() !== '') {
        isFilterSet = true;
      } else {
        delete searchTerms[col];
      }
    }

    console.log(searchTerms);

    let nameSearch = () => {
      let found = false;
      if (isFilterSet) {
        for (const col in searchTerms) {
          searchTerms[col].trim().toLowerCase().split(' ').forEach(word => {
            if (data[col].toString().toLowerCase().indexOf(word) != -1 && isFilterSet) {
              found = true
            }
          });
        }
        return found
      } else {
        return true;
      }
    }
    return nameSearch()
  }
  return filterFunction
}

updateReeferControls() {
  console.log("Click >>>>>"+event +" Failure Rate:"+this.failureRate+" update Frequency:"+this.updateFrequency);
  const request = {};
  request['failureRate'] = this.failureRate.toString();
  request['updateFrequency'] = this.updateFrequency.toString();

  this.restService.updateReeferControls(request).subscribe((data) => {
    console.log(data);

  });
}
createAnomaly() {
  this.restService.generateAnomaly().subscribe((data) => {
    console.log(data);

  });
}
// Reset table filters
resetFilters() {
  this.filterValues = {}
  this.filterSelectObj.forEach((value, key) => {
    value.modelValue = undefined;
  })
  this.reeferDataSource.filter = "";
}

  addPortReefers(port : any) {
    console.log("Add Port Reefers - port:"+port.name);
    this.restService.addReefers(port.name).subscribe((data) => {
      console.log(data);

      this.portsDataSource.data = data;
      this.onRowClicked(port);
      });
  }
  onRowClicked(reefer: any): void {
    console.log(reefer);
    this.selectedPort = reefer.port;
    this.restService.getReefers(reefer.port).subscribe((data) => {
      console.log(data);

      this.reeferDataSource.data = data;
      });

  }


}
