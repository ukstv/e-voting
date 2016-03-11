/******************************************************************************
 * e-voting system                                                            *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 * *
 * This program is free software; you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation; either version 2 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied                         *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * *
 * You can find copy of the GNU General Public License in LICENSE.txt file    *
 * at the top-level directory of this distribution.                           *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/

'use strict';

angular
  .module('e-voting.role-manager', [])
  .service('roleManager', ['$sessionStorage', '$state',
    function ($sessionStorage, $state) {
      return {
        checkAccess: checkAccess
      };
      function checkAccess(event, toState, toParams, fromState, fromParams) {
        if (toState.access !== undefined) {
          if (toState.access.loginRequired !== undefined && toState.access.loginRequired) {
            if (angular.isUndefined($sessionStorage.cookie)) {
              event.preventDefault();
              $state.go('signIn', {location: "replace", reload: false, inherit: false, notify: false});
            }
          }
        }
      }
    }
  ]);