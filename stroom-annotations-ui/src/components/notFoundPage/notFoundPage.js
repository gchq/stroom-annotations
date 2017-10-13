import React from 'react'

import AppBar from 'material-ui/AppBar'
import RaisedButton from 'material-ui/RaisedButton'
import Paper from 'material-ui/Paper'

import '../appStyle/app.css'

const NotFoundPage = (props) => (
    <div className='app'>
        <AppBar
            title='Page Not Found'
            iconElementLeft={<div />}
            />
        <Paper className='app--body' zDepth={0}>
            <RaisedButton
                label="I'll get me coat..."
                primary={true}
                onClick={(e) => props.history.push('/')}
                />
        </Paper>
    </div>
)

export default NotFoundPage