import React, { Component } from 'react'
import PropTypes from 'prop-types'

import IconButton from 'material-ui/IconButton'
import BackIcon from 'material-ui/svg-icons/navigation/arrow-back'
import AppBar from 'material-ui/AppBar'
import Paper from 'material-ui/Paper'

import ApiCallSpinner from '../apiCallSpinner'
import ErrorDisplay from '../errorDisplay'
import SnackbarDisplay from '../snackbarDisplay'

import History from '../history'

class AnnotationHistoryPage extends Component {
    componentDidMount() {
        this.props.fetchAnnotationHistory(this.props.annotationId)
    }

    handleNavigateBack() {
        if (this.props.allowNavigation) {
            this.props.history.push(`/singleWithNav/${this.props.indexUuid}/${this.props.annotationId}`)
        } else {
            this.props.history.push(`/single/${this.props.indexUuid}/${this.props.annotationId}`)
        }
    }

    render() {
        // Only present navigation icon if we are NOT a dialog
        let iconElementLeft = <IconButton><BackIcon /></IconButton>

        // Indicate if the annotation information is clean
        let title = `Annotation on ${this.props.annotationId}`

        return (
            <div className='app'>
                <AppBar
                    title={title}
                    iconElementLeft={iconElementLeft}
                    onLeftIconButtonTouchTap={this.handleNavigateBack.bind(this)}
                    iconElementRight={<ErrorDisplay />}
                    />
                <SnackbarDisplay />
                <ApiCallSpinner />
                <Paper className='app--body' zDepth={0}>
                    <History annotationId={this.props.annotationId}/>
                </Paper>
            </div>
        );
    }
}

AnnotationHistoryPage.propTypes = {
    indexUuid: PropTypes.string.isRequired,
    annotationId: PropTypes.string.isRequired,
    allowNavigation: PropTypes.bool.isRequired,

    history: PropTypes.object.isRequired
}

export default AnnotationHistoryPage