import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import AnnotationHistoryPage from './annotationHistoryPage'

import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'

export default connect(
    (state) => ({
        annotationId: state.ui.annotationId,
        allowNavigation: state.ui.allowNavigation
    }),
    {
        fetchAnnotationHistory
    }
)(withRouter(AnnotationHistoryPage));
