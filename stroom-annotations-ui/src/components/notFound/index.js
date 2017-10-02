import React from 'react'
import { withRouter } from 'react-router'

import AppBar from 'material-ui/AppBar'

import '../appStyle/app.css'

const NotFound = (props) => (
    <div className='app'>
        <AppBar
            title='Page Not Found'
            onLeftIconButtonTouchTap={() => props.history.push('/')}
            />
    </div>
)

export default withRouter(NotFound)