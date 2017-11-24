import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import ManageAnnotationsPage from './manageAnnotationsPage'

import {
    searchAnnotations,
    moreAnnotations
} from '../../actions/searchAnnotations'

export default connect(
    (state) => ({
        indexUuid: state.ui.indexUuid,
        canRequestMore: state.manageAnnotations.canRequestMore,
        annotations: state.manageAnnotations.annotations,
        searchTerm: state.manageAnnotations.searchTerm
    }),
    {
        searchAnnotations,
        moreAnnotations
    }
)(withRouter(ManageAnnotationsPage));